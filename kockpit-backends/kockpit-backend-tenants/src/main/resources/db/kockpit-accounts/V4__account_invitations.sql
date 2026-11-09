-- Invitations to create an account: an administrator creates one (no tenant), the invited person signs up with it
-- and may then create a tenant. Sign-up also accepts a tenant's invitation: that account joins the tenant only.
alter table tenant_invitations alter column tenant_id drop not null;

-- Off for accounts created with a tenant's invitation; accounts that exist already keep the right.
alter table users add column can_create_tenants boolean not null default true;
