output "manifest_json" {
  description = "The generated manifest as a JSON string"
  value       = local.manifest_json
}

output "manifest" {
  description = "The generated manifest as a Terraform object"
  value       = local.manifest
}

output "output_path" {
  description = "Path to the written manifest file"
  value       = local_file.manifest.filename
}

output "s3_uri" {
  description = "S3 URI of the uploaded manifest (if s3_bucket was set)"
  value       = var.s3_bucket != null ? "s3://${var.s3_bucket}/${local.resolved_s3_key}" : null
}