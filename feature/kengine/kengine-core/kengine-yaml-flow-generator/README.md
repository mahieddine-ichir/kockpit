Generate flows from YAML files.

Usage:
```properties
kengine.yaml.flow.enable=true
kengine.yaml.flow.location=/flows/ #folders to scan, located in classpath resources
```
```yaml
name: FLOW_EXAMPLE
details: flow details
rules:
  - name: BR_DO_STUFF
    details: rule details
    actions:
      - ACT_DO_SOMETHING
      - ACT_DO_SOMETHING2
    predicate:
      names:
        - PRE_IS_OK
      ok:
        actions:
          - ACT_DO_IF_OK
      ko:
        actions:
          - ACT_DO_IF_NOK
      lastly:
        actions:
          - ACT_DO_ANYWAYS
```