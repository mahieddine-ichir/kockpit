package org.kockpit.audit.httpexchange.obfuscator;

import java.net.URI;

public interface AuditObfuscator {
  String obfuscateBody(URI uri, String body);
}
