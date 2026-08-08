-- What to Cook? database schema.
-- The Android application has read-only access through the anon role.

create schema if not exists private;

create table if not exists public.ingredients (
    id bigint generated always as identity primary key,
    slug text not null unique,
    name text not null,
    emoji text not null default '',
    category text not null,
    search_terms text[] not null default '{}',
    is_featured boolean not null default false,
    sort_order smallint not null default 100,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint ingredients_slug_format check (slug ~ '^[a-z0-9]+(?:_[a-z0-9]+)*$'),
    constraint ingredients_name_not_blank check (btrim(name) <> ''),
    constraint ingredients_category_allowed check (
        category in ('Protein', 'Dairy', 'Vegetables', 'Fruit', 'Grains', 'Pantry', 'Herbs and spices', 'Other')
    )
);

create table if not exists public.recipes (
    id bigint generated always as identity primary key,
    slug text not null unique,
    title text not null,
    emoji text not null default '🍽️',
    description text not null,
    total_minutes smallint not null,
    difficulty text not null,
    servings smallint not null,
    published boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint recipes_slug_format check (slug ~ '^[a-z0-9]+(?:_[a-z0-9]+)*$'),
    constraint recipes_title_not_blank check (btrim(title) <> ''),
    constraint recipes_description_not_blank check (btrim(description) <> ''),
    constraint recipes_total_minutes_positive check (total_minutes > 0),
    constraint recipes_servings_positive check (servings > 0),
    constraint recipes_difficulty_allowed check (difficulty in ('Very easy', 'Easy', 'Medium', 'Advanced'))
);

create table if not exists public.recipe_ingredients (
    recipe_id bigint not null references public.recipes(id) on delete cascade,
    ingredient_id bigint not null references public.ingredients(id) on delete restrict,
    position smallint not null,
    display_text text not null,
    required_for_match boolean not null default true,
    optional boolean not null default false,
    primary key (recipe_id, ingredient_id),
    unique (recipe_id, position),
    constraint recipe_ingredients_position_positive check (position > 0),
    constraint recipe_ingredients_display_not_blank check (btrim(display_text) <> ''),
    constraint recipe_ingredients_optional_not_required check (not (optional and required_for_match))
);

create table if not exists public.recipe_steps (
    recipe_id bigint not null references public.recipes(id) on delete cascade,
    step_number smallint not null,
    instruction text not null,
    primary key (recipe_id, step_number),
    constraint recipe_steps_number_positive check (step_number > 0),
    constraint recipe_steps_instruction_not_blank check (btrim(instruction) <> '')
);

create table if not exists private.recipe_sources (
    id bigint generated always as identity primary key,
    recipe_id bigint not null references public.recipes(id) on delete cascade,
    source_key text not null unique,
    source_type text not null,
    source_name text not null,
    source_url text,
    license_name text,
    attribution text,
    editorial_notes text,
    reviewed_at timestamptz,
    created_at timestamptz not null default now(),
    constraint recipe_sources_type_allowed check (source_type in ('original', 'adapted', 'licensed')),
    constraint recipe_sources_name_not_blank check (btrim(source_name) <> '')
);

create index if not exists recipe_ingredients_ingredient_id_idx
    on public.recipe_ingredients (ingredient_id, recipe_id);
create index if not exists recipe_ingredients_required_idx
    on public.recipe_ingredients (recipe_id, ingredient_id)
    where required_for_match;
create index if not exists recipes_published_sort_idx
    on public.recipes (total_minutes, title)
    where published;
create index if not exists ingredients_active_sort_idx
    on public.ingredients (is_featured desc, sort_order, name)
    where active;
create index if not exists recipe_sources_recipe_id_idx
    on private.recipe_sources (recipe_id);

alter table public.ingredients enable row level security;
alter table public.recipes enable row level security;
alter table public.recipe_ingredients enable row level security;
alter table public.recipe_steps enable row level security;
alter table private.recipe_sources enable row level security;

drop policy if exists ingredients_public_read on public.ingredients;
create policy ingredients_public_read
    on public.ingredients for select
    to anon, authenticated
    using (active);

drop policy if exists recipes_public_read on public.recipes;
create policy recipes_public_read
    on public.recipes for select
    to anon, authenticated
    using (published);

drop policy if exists recipe_ingredients_public_read on public.recipe_ingredients;
create policy recipe_ingredients_public_read
    on public.recipe_ingredients for select
    to anon, authenticated
    using (
        exists (
            select 1
            from public.recipes
            where recipes.id = recipe_ingredients.recipe_id
              and recipes.published
        )
    );

drop policy if exists recipe_steps_public_read on public.recipe_steps;
create policy recipe_steps_public_read
    on public.recipe_steps for select
    to anon, authenticated
    using (
        exists (
            select 1
            from public.recipes
            where recipes.id = recipe_steps.recipe_id
              and recipes.published
        )
    );

revoke all on table public.ingredients from anon, authenticated;
revoke all on table public.recipes from anon, authenticated;
revoke all on table public.recipe_ingredients from anon, authenticated;
revoke all on table public.recipe_steps from anon, authenticated;
grant select on table public.ingredients to anon, authenticated;
grant select on table public.recipes to anon, authenticated;
grant select on table public.recipe_ingredients to anon, authenticated;
grant select on table public.recipe_steps to anon, authenticated;

revoke all on schema private from public, anon, authenticated;
grant usage on schema private to service_role;
grant all on all tables in schema private to service_role;

create or replace function public.match_recipes(available_ingredient_slugs text[])
returns table (
    recipe_slug text,
    title text,
    emoji text,
    description text,
    total_minutes smallint,
    difficulty text,
    servings smallint,
    required_ingredient_slugs text[],
    ingredient_lines text[],
    steps text[]
)
language sql
stable
security invoker
set search_path = ''
as $$
    select
        r.slug,
        r.title,
        r.emoji,
        r.description,
        r.total_minutes,
        r.difficulty,
        r.servings,
        array(
            select i.slug
            from public.recipe_ingredients ri
            join public.ingredients i on i.id = ri.ingredient_id
            where ri.recipe_id = r.id
              and ri.required_for_match
            order by ri.position
        ),
        array(
            select ri.display_text
            from public.recipe_ingredients ri
            where ri.recipe_id = r.id
            order by ri.position
        ),
        array(
            select rs.instruction
            from public.recipe_steps rs
            where rs.recipe_id = r.id
            order by rs.step_number
        )
    from public.recipes r
    where r.published
      and coalesce(cardinality(available_ingredient_slugs), 0) > 0
      and exists (
          select 1
          from public.recipe_ingredients ri
          where ri.recipe_id = r.id
            and ri.required_for_match
      )
      and not exists (
          select 1
          from public.recipe_ingredients ri
          join public.ingredients i on i.id = ri.ingredient_id
          where ri.recipe_id = r.id
            and ri.required_for_match
            and not (i.slug = any(available_ingredient_slugs))
      )
    order by (
        select count(*)
        from public.recipe_ingredients ri
        where ri.recipe_id = r.id
          and ri.required_for_match
    ) desc,
    r.total_minutes,
    r.title;
$$;

revoke execute on function public.match_recipes(text[]) from public;
grant execute on function public.match_recipes(text[]) to anon, authenticated;
