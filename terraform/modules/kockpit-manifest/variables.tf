variable "domain" {
  description = "Domain identifier (e.g. wcplatform)"
  type        = string
}

variable "env" {
  description = "Environment (e.g. dev, staging, production)"
  type        = string
}

variable "name" {
  description = "Human-readable name for this manifest"
  type        = string
}

variable "output_path" {
  description = "Local path where the manifest JSON file will be written"
  type        = string
  default     = null
}

variable "s3_bucket" {
  description = "S3 bucket name to upload the manifest to (optional, AWS only)"
  type        = string
  default     = null
}

variable "s3_key" {
  description = "S3 object key for the manifest (defaults to <domain>-<env>-manifest.json, AWS only)"
  type        = string
  default     = null
}

variable "azure_storage_account" {
  description = "Azure Storage Account name to upload the manifest to (optional, Azure only)"
  type        = string
  default     = null
}

variable "azure_storage_container" {
  description = "Azure Blob Storage container name (optional, Azure only)"
  type        = string
  default     = null
}

variable "azure_blob_name" {
  description = "Azure blob name for the manifest (defaults to <domain>-<env>-manifest.json, Azure only)"
  type        = string
  default     = null
}

# ── Feature Flipping ─────────────────────────────────────────────────────────

variable "feature_flipping_services" {
  description = "List of feature-flipping service definitions"
  type = list(object({
    name = string
    id   = string
    keys = list(object({
      key            = string
      enabled        = bool
      expirationDate = optional(string)
      comment        = optional(string)
    }))
  }))
  default = []
}

# ── Dyna Config ───────────────────────────────────────────────────────────────

variable "dyna_config_services" {
  description = "List of dyna-config service definitions"
  type = list(object({
    name = string
    id   = string
    keys = list(object({
      key         = string
      value       = any
      description = optional(string)
    }))
  }))
  default = []
}

# ── Audit ─────────────────────────────────────────────────────────────────────

variable "audit_services" {
  description = "List of audit service definitions"
  type = list(object({
    name    = string
    id      = string
    columns = list(string)
    search_columns = list(object({
      name    = string
      type    = string
      path    = optional(string)
      options = optional(list(any))
    }))
    saved_filters = optional(list(object({
      name = string
      filters = list(object({
        name   = string
        path   = string
        values = any
      }))
    })), [])
  }))
  default = []
}