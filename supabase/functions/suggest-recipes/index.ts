import { createClient } from "@supabase/supabase-js";

const OPENAI_MODEL = "gpt-5-mini";
const DEVICE_DAILY_LIMIT = 5;
const GLOBAL_DAILY_LIMIT = 200;
const MAX_SELECTED_INGREDIENTS = 30;
const STAPLES = new Set(["salt", "black_pepper", "oil", "water"]);

const headers = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json",
};

type Mode = "strict" | "ideas";
type IngredientSource = "available" | "staple" | "missing";

type AiIngredient = {
  name: string;
  quantity: string;
  slug: string;
  source: IngredientSource;
};

type AiRecipe = {
  title: string;
  emoji: string;
  description: string;
  total_minutes: number;
  difficulty: "Very easy" | "Easy" | "Medium";
  servings: number;
  ingredients: AiIngredient[];
  steps: string[];
};

const recipeSchema = {
  type: "object",
  additionalProperties: false,
  required: ["recipes"],
  properties: {
    recipes: {
      type: "array",
      minItems: 1,
      maxItems: 3,
      items: {
        type: "object",
        additionalProperties: false,
        required: [
          "title", "emoji", "description", "total_minutes", "difficulty",
          "servings", "ingredients", "steps",
        ],
        properties: {
          title: { type: "string", minLength: 3, maxLength: 80 },
          emoji: { type: "string", minLength: 1, maxLength: 8 },
          description: { type: "string", minLength: 10, maxLength: 180 },
          total_minutes: { type: "integer", minimum: 1, maximum: 360 },
          difficulty: { type: "string", enum: ["Very easy", "Easy", "Medium"] },
          servings: { type: "integer", minimum: 1, maximum: 8 },
          ingredients: {
            type: "array",
            minItems: 1,
            maxItems: 20,
            items: {
              type: "object",
              additionalProperties: false,
              required: ["name", "quantity", "slug", "source"],
              properties: {
                name: { type: "string", minLength: 1, maxLength: 60 },
                quantity: { type: "string", minLength: 1, maxLength: 60 },
                slug: { type: "string", minLength: 1, maxLength: 80 },
                source: { type: "string", enum: ["available", "staple", "missing"] },
              },
            },
          },
          steps: {
            type: "array",
            minItems: 1,
            maxItems: 12,
            items: { type: "string", minLength: 5, maxLength: 500 },
          },
        },
      },
    },
  },
};

function json(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), { status, headers });
}

function secretKey(): string {
  const modern = Deno.env.get("SUPABASE_SECRET_KEYS");
  if (modern) {
    const keys = JSON.parse(modern);
    if (typeof keys.default === "string") return keys.default;
    const first = Object.values(keys).find((value) => typeof value === "string");
    if (typeof first === "string") return first;
  }
  return Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
}

function hasValidClientKey(request: Request): boolean {
  const supplied = request.headers.get("apikey") ?? "";
  if (!supplied) return false;
  const modern = Deno.env.get("SUPABASE_PUBLISHABLE_KEYS");
  if (modern) {
    const keys = JSON.parse(modern);
    if (Object.values(keys).includes(supplied)) return true;
  }
  return supplied === (Deno.env.get("SUPABASE_ANON_KEY") ?? "");
}

async function sha256(value: string): Promise<string> {
  const bytes = new TextEncoder().encode(value);
  const digest = await crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(digest))
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
}

function outputText(payload: Record<string, unknown>): string {
  if (typeof payload.output_text === "string") return payload.output_text;
  const output = Array.isArray(payload.output) ? payload.output : [];
  for (const item of output) {
    if (!item || typeof item !== "object" || !Array.isArray((item as { content?: unknown }).content)) continue;
    for (const content of (item as { content: unknown[] }).content) {
      if (content && typeof content === "object" && typeof (content as { text?: unknown }).text === "string") {
        return (content as { text: string }).text;
      }
    }
  }
  throw new Error("OpenAI returned no recipe text");
}

function normalizeRecipes(
  recipes: AiRecipe[],
  mode: Mode,
  selected: Map<string, string>,
): Record<string, unknown>[] {
  const normalized: Record<string, unknown>[] = [];
  for (const recipe of recipes) {
    const usedAvailable = new Set<string>();
    const missing: string[] = [];
    const lines: string[] = [];
    let valid = true;

    for (const ingredient of recipe.ingredients) {
      const slug = ingredient.slug.trim().toLowerCase();
      if (ingredient.source === "available") {
        if (!selected.has(slug)) {
          valid = false;
          break;
        }
        usedAvailable.add(slug);
      } else if (ingredient.source === "staple") {
        if (!STAPLES.has(slug)) {
          valid = false;
          break;
        }
      } else {
        if (mode === "strict") {
          valid = false;
          break;
        }
        missing.push(ingredient.name.trim());
      }
      lines.push(`${ingredient.quantity.trim()} ${ingredient.name.trim()}`.trim());
    }

    if (!valid || usedAvailable.size === 0 || (mode === "ideas" && missing.length > 5)) continue;

    const idSeed = `${mode}|${recipe.title}|${Array.from(selected.keys()).join(",")}`;
    normalized.push({
      recipe_slug: `ai_${(idSeed.toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_|_$/g, "")).slice(0, 90)}`,
      title: recipe.title.trim(),
      emoji: recipe.emoji.trim(),
      description: recipe.description.trim(),
      total_minutes: recipe.total_minutes,
      difficulty: recipe.difficulty,
      servings: recipe.servings,
      source_name: "AI-generated cooking idea",
      source_url: null,
      license_name: null,
      attribution: "Generated for Cook From This",
      modified_from_source: false,
      ai_generated: true,
      required_ingredient_slugs: Array.from(usedAvailable),
      missing_ingredients: Array.from(new Set(missing)),
      ingredient_lines: lines,
      steps: recipe.steps.map((step) => step.trim()),
    });
  }
  return normalized;
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") return new Response("ok", { headers });
  if (request.method !== "POST") return json(405, { error: "Method not allowed" });
  if (!hasValidClientKey(request)) return json(401, { error: "Invalid client key" });

  const openAiKey = Deno.env.get("OPENAI_API_KEY") ?? "";
  const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
  const adminKey = secretKey();
  if (!openAiKey || !supabaseUrl || !adminKey) return json(503, { error: "AI service is not configured" });

  let body: Record<string, unknown>;
  try {
    body = await request.json();
  } catch {
    return json(400, { error: "Invalid JSON body" });
  }

  const mode: Mode = body.mode === "ideas" ? "ideas" : "strict";
  const installationId = typeof body.installation_id === "string" ? body.installation_id.trim() : "";
  const requestedSlugs = Array.isArray(body.ingredient_slugs)
    ? Array.from(new Set(body.ingredient_slugs.filter((value): value is string => typeof value === "string")
      .map((value) => value.trim().toLowerCase()).filter(Boolean))).sort()
    : [];

  if (!/^[0-9a-f-]{36}$/i.test(installationId)) return json(400, { error: "Invalid installation ID" });
  if (requestedSlugs.length < 1 || requestedSlugs.length > MAX_SELECTED_INGREDIENTS) {
    return json(400, { error: "Choose between 1 and 30 ingredients" });
  }

  const supabase = createClient(supabaseUrl, adminKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const { data: ingredients, error: ingredientError } = await supabase
    .from("ingredients")
    .select("slug,name")
    .eq("active", true)
    .in("slug", requestedSlugs);
  if (ingredientError) return json(503, { error: "Ingredient catalogue is unavailable" });

  const selected = new Map<string, string>((ingredients ?? []).map((item) => [item.slug, item.name]));
  if (selected.size !== requestedSlugs.length) return json(400, { error: "Unknown ingredient selected" });

  const cacheKey = await sha256(`${mode}|${requestedSlugs.join(",")}`);
  const { data: cached } = await supabase
    .from("ai_recipe_cache")
    .select("response,hit_count")
    .eq("cache_key", cacheKey)
    .gt("expires_at", new Date().toISOString())
    .maybeSingle();
  if (cached?.response) {
    await supabase.from("ai_recipe_cache").update({ hit_count: (cached.hit_count ?? 0) + 1 })
      .eq("cache_key", cacheKey);
    return json(200, { ...cached.response, cached: true, remaining_today: null });
  }

  const installationHash = await sha256(installationId);
  const { data: quotaRows, error: quotaError } = await supabase.rpc("consume_ai_recipe_quota", {
    p_installation_hash: installationHash,
    p_device_limit: DEVICE_DAILY_LIMIT,
    p_global_limit: GLOBAL_DAILY_LIMIT,
  });
  const quota = quotaRows?.[0];
  if (quotaError || !quota) return json(503, { error: "AI quota is temporarily unavailable" });
  if (!quota.allowed) {
    return json(429, {
      error: "Daily AI limit reached. Saved recipes are still available.",
      remaining_today: quota.device_remaining,
    });
  }

  const available = Array.from(selected.entries()).map(([slug, name]) => `${slug}: ${name}`).join("\n");
  const modeRule = mode === "strict"
    ? "STRICT MODE: Use only the available ingredients and the four assumed staples. Never add a missing ingredient."
    : "IDEAS MODE: Use the available ingredients as the foundation. You may add at most 5 ordinary missing ingredients, and must label every one as missing.";
  const prompt = `You are the cooking engine for Cook From This. Create 1 to 3 honest, practical recipes in English.

${modeRule}

Available ingredients (use these exact slugs for source=available):
${available}

Assumed staples (the user always has these; use exact slug and source=staple):
salt, black_pepper, oil, water

Rules:
- Every ingredient used in a direction must appear in the structured ingredients list.
- Mark each ingredient source accurately as available, staple, or missing.
- In strict mode, source=missing is forbidden.
- Prefer using several available ingredients, but do not force combinations that taste bad.
- Do not invent multiple names for essentially the same preparation when choices are limited.
- Quantities and cooking times must be realistic.
- Include essential food-safety instructions. Poultry must be fully cooked to 74 C / 165 F internally.
- Never claim the recipe came from a human author or website.`;

  try {
    const openAiResponse = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${openAiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: OPENAI_MODEL,
        input: prompt,
        reasoning: { effort: "low" },
        text: {
          format: {
            type: "json_schema",
            name: "recipe_suggestions",
            strict: true,
            schema: recipeSchema,
          },
        },
        max_output_tokens: 3000,
        store: false,
      }),
    });

    if (!openAiResponse.ok) throw new Error(`OpenAI HTTP ${openAiResponse.status}`);
    const openAiPayload = await openAiResponse.json();
    const parsed = JSON.parse(outputText(openAiPayload)) as { recipes?: AiRecipe[] };
    const recipes = normalizeRecipes(parsed.recipes ?? [], mode, selected);
    if (recipes.length === 0) throw new Error("No recipe passed ingredient validation");

    const response = { mode, recipes, generated_at: new Date().toISOString() };
    const { error: cacheError } = await supabase.from("ai_recipe_cache").upsert({
      cache_key: cacheKey,
      mode,
      ingredient_slugs: requestedSlugs,
      response,
      expires_at: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000).toISOString(),
    }, { onConflict: "cache_key" });
    if (cacheError) console.error("AI cache write failed", cacheError.message);

    return json(200, {
      ...response,
      cached: false,
      remaining_today: quota.device_remaining,
    });
  } catch (error) {
    await supabase.rpc("refund_ai_recipe_quota", { p_installation_hash: installationHash });
    console.error("AI recipe generation failed", error instanceof Error ? error.message : "Unknown error");
    return json(502, { error: "AI could not create a safe recipe right now. Please try again." });
  }
});
