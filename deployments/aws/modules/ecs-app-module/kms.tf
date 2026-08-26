# KMS key
resource "aws_kms_key" "app_kms_key" {
  description             = "kms_key-${var.stack}-${var.env}"
  enable_key_rotation     = true
  key_usage               = "ENCRYPT_DECRYPT"
  deletion_window_in_days = 7

  policy = <<EOF
{
  "Id": "Allow KMS for Root and Sec",
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Allow use of key by producers",
      "Effect": "Allow",
      "Principal": {
        "AWS": [
                "${aws_iam_role.ecs_task.arn}"
                 ]
      },
      "Action": [
            "kms:Decrypt",
            "kms:Encrypt",
            "kms:GenerateDataKey*"
       ],
      "Resource": "*"
    },
    {
      "Sid": "Enable IAM User Permissions",
      "Effect": "Allow",
      "Action": [ "kms:*" ],
      "Resource": [ "*" ],
      "Principal": {
        "AWS": [
                 "arn:aws:iam::${var.account_id}:root"
               ]
      }
    }
  ]
}
EOF

  tags = var.tags
}

# KMS alias
resource "aws_kms_alias" "wcp_kms_key" {
  name          = "alias/${var.application}/${var.env}"
  target_key_id = aws_kms_key.app_kms_key.key_id
}

