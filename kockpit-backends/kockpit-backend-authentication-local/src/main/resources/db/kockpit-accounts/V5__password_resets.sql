-- Password reset links: an administrator creates one and sends it (kockpit sends no mail), the account's owner
-- chooses a new password with it. Like API keys, only the code's SHA-256 is stored.
create table password_resets (
    id         uuid        primary key,
    user_id    uuid        not null references users (id) on delete cascade,
    code_hash  char(64)    not null unique,
    created_by uuid        references users (id) on delete set null,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null,
    used_at    timestamptz
);

create index password_resets_user_idx on password_resets (user_id);

-- Login tokens issued before this are refused: set when the password changes.
alter table users add column sessions_valid_after timestamptz;
