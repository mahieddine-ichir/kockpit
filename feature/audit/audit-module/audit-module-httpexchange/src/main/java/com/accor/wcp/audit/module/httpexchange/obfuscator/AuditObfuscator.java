package com.accor.wcp.audit.module.httpexchange.obfuscator;

import java.net.URI;

public interface AuditObfuscator {
  String obfuscateBody(URI uri, String body);
}
