create table if not exists public.ai_recipe_cache (
    id bigint generated always as identity primary key,
    cache_key text not null unique,
    mode text not null check (mode in ('strict', 'ideas')),
    ingredient_slugs text[] not null,
    response jsonb not null,
    hit_count integer not null default 0 check (hit_count >= 0),
    created_at timestamptz not null default now(),
    expires_at timestamptz not null default (now() + interval '90 days')
);

create index if not exists ai_recipe_cache_lookup_idx
    on public.ai_recipe_cache (cache_key, expires_at);

create table if not exists public.ai_recipe_device_usage (
    usage_date date not null,
    installation_hash text not null,
    request_count integer not null default 0 check (request_count >= 0),
    primary key (usage_date, installation_hash)
);

create table if not exists public.ai_recipe_global_usage (
    usage_date date primary key,
    request_count integer not null default 0 check (request_count >= 0)
);

alter table public.ai_recipe_cache enable row level security;
alter table public.ai_recipe_device_usage enable row level security;
alter table public.ai_recipe_global_usage enable row level security;

revoke all on table public.ai_recipe_cache from anon, authenticated;
revoke all on table public.ai_recipe_device_usage from anon, authenticated;
revoke all on table public.ai_recipe_global_usage from anon, authenticated;

grant select, insert, update on table public.ai_recipe_cache to service_role;
grant select, insert, update, delete on table public.ai_recipe_device_usage to service_role;
grant select, insert, update, delete on table public.ai_recipe_global_usage to service_role;
grant usage, select on sequence public.ai_recipe_cache_id_seq to service_role;

create or replace function public.consume_ai_recipe_quota(
    p_installation_hash text,
    p_device_limit integer default 5,
    p_global_limit integer default 200
)
returns table (
    allowed boolean,
    device_remaining integer,
    global_remaining integer
)
language plpgsql
security definer
set search_path = ''
as $$
declare
    today date := current_date;
    device_count integer;
    global_count integer;
begin
    if p_installation_hash is null
       or length(p_installation_hash) <> 64
       or p_device_limit < 1
       or p_global_limit < 1 then
        raise exception 'Invalid AI quota request';
    end if;

    perform pg_advisory_xact_lock(hashtext('what-to-cook-ai-' || today::text));

    delete from public.ai_recipe_device_usage
     where usage_date < today - 31;
    delete from public.ai_recipe_global_usage
     where usage_date < today - 31;

    select coalesce(request_count, 0)
      into device_count
      from public.ai_recipe_device_usage
     where usage_date = today
       and installation_hash = p_installation_hash;
    device_count := coalesce(device_count, 0);

    select coalesce(request_count, 0)
      into global_count
      from public.ai_recipe_global_usage
     where usage_date = today;
    global_count := coalesce(global_count, 0);

    if device_count >= p_device_limit or global_count >= p_global_limit then
        return query select false,
            greatest(p_device_limit - device_count, 0),
            greatest(p_global_limit - global_count, 0);
        return;
    end if;

    insert into public.ai_recipe_device_usage (usage_date, installation_hash, request_count)
    values (today, p_installation_hash, 1)
    on conflict (usage_date, installation_hash)
    do update set request_count = public.ai_recipe_device_usage.request_count + 1;

    insert into public.ai_recipe_global_usage (usage_date, request_count)
    values (today, 1)
    on conflict (usage_date)
    do update set request_count = public.ai_recipe_global_usage.request_count + 1;

    return query select true,
        p_device_limit - device_count - 1,
        p_global_limit - global_count - 1;
end;
$$;

create or replace function public.refund_ai_recipe_quota(p_installation_hash text)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    today date := current_date;
begin
    perform pg_advisory_xact_lock(hashtext('what-to-cook-ai-' || today::text));

    update public.ai_recipe_device_usage
       set request_count = greatest(request_count - 1, 0)
     where usage_date = today
       and installation_hash = p_installation_hash;

    update public.ai_recipe_global_usage
       set request_count = greatest(request_count - 1, 0)
     where usage_date = today;
end;
$$;

revoke all on function public.consume_ai_recipe_quota(text, integer, integer) from public, anon, authenticated;
revoke all on function public.refund_ai_recipe_quota(text) from public, anon, authenticated;
grant execute on function public.consume_ai_recipe_quota(text, integer, integer) to service_role;
grant execute on function public.refund_ai_recipe_quota(text) to service_role;
