package com.accor.wcp.sample.obfuscationlib;

import com.accor.wcp.sample.obfuscationlib.model.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
class ObfuscateSampleLibraryController {

  private final ObfuscateSampleLibraryService obfuscateSampleLibraryService;

  @GetMapping(value = "/obfuscate/user")
  public ResponseEntity<User> obfuscateUserData() {
    return ResponseEntity.ok(obfuscateSampleLibraryService.obfuscateUser());
  }

  @PostMapping(value = "/obfuscate/value")
  @ResponseBody
  public ResponseEntity<String> obfuscateUser(
      @RequestBody String body, @RequestParam("masker") String masker) {
    return ResponseEntity.ok(obfuscateSampleLibraryService.obfuscateValue(body, masker));
  }

  @PostMapping(value = "/obfuscate/json")
  @ResponseBody
  public ResponseEntity<String> postObfuscateJson(
      @RequestParam("path") List<String> path,
      @RequestParam("masker") List<String> masker,
      @RequestBody String body) {
    return ResponseEntity.ok(obfuscateSampleLibraryService.obfuscateJson(path, masker, body));
  }

  @PostMapping(value = "/obfuscate/xml")
  @ResponseBody
  public ResponseEntity<String> obfuscateXml(
      @RequestParam("path") List<String> path,
      @RequestParam("masker") List<String> masker,
      @RequestBody String body) {
    return ResponseEntity.ok(obfuscateSampleLibraryService.obfuscateXml(path, masker, body));
  }
}
