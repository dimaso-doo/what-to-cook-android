-- Separates display lines from exact ingredient requirements and exposes attribution.

alter table public.recipes
    add column if not exists source_name text,
    add column if not exists source_url text,
    add column if not exists license_name text,
    add column if not exists attribution text,
    add column if not exists modified_from_source boolean not null default false;

update public.recipes
set source_name = 'Dimaso original recipe',
    source_url = 'https://github.com/dimaso-doo/what-to-cook-android',
    license_name = 'All rights reserved',
    attribution = 'Dimaso d.o.o.',
    modified_from_source = false
where source_name is null;

alter table public.recipe_ingredients
    add column if not exists id bigint generated always as identity;
alter table public.recipe_ingredients
    drop constraint if exists recipe_ingredients_pkey;
alter table public.recipe_ingredients
    add constraint recipe_ingredients_pkey primary key (id);

create table if not exists public.recipe_requirements (
    recipe_id bigint not null references public.recipes(id) on delete cascade,
    ingredient_id bigint not null references public.ingredients(id) on delete restrict,
    primary key (recipe_id, ingredient_id)
);

create index if not exists recipe_requirements_ingredient_id_idx
    on public.recipe_requirements (ingredient_id, recipe_id);

insert into public.recipe_requirements (recipe_id, ingredient_id)
select distinct recipe_id, ingredient_id
from public.recipe_ingredients
where required_for_match
on conflict (recipe_id, ingredient_id) do nothing;

alter table public.recipe_requirements enable row level security;

drop policy if exists recipe_requirements_public_read on public.recipe_requirements;
create policy recipe_requirements_public_read
    on public.recipe_requirements for select
    to anon, authenticated
    using (
        exists (
            select 1
            from public.recipes
            where recipes.id = recipe_requirements.recipe_id
              and recipes.published
        )
    );

revoke all on table public.recipe_requirements from anon, authenticated;
grant select on table public.recipe_requirements to anon, authenticated;

drop function if exists public.match_recipes(text[]);

create or replace function public.match_recipes(available_ingredient_slugs text[])
returns table (
    recipe_slug text,
    title text,
    emoji text,
    description text,
    total_minutes smallint,
    difficulty text,
    servings smallint,
    source_name text,
    source_url text,
    license_name text,
    attribution text,
    modified_from_source boolean,
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
        r.source_name,
        r.source_url,
        r.license_name,
        r.attribution,
        r.modified_from_source,
        array(
            select i.slug
            from public.recipe_requirements rr
            join public.ingredients i on i.id = rr.ingredient_id
            where rr.recipe_id = r.id
            order by i.name
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
          from public.recipe_requirements rr
          where rr.recipe_id = r.id
      )
      and not exists (
          select 1
          from public.recipe_requirements rr
          join public.ingredients i on i.id = rr.ingredient_id
          where rr.recipe_id = r.id
            and not (i.slug = any(available_ingredient_slugs))
      )
    order by (
        select count(*)
        from public.recipe_requirements rr
        where rr.recipe_id = r.id
    ) desc,
    r.total_minutes,
    r.title;
$$;

revoke execute on function public.match_recipes(text[]) from public;
grant execute on function public.match_recipes(text[]) to anon, authenticated;
