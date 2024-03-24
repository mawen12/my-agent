
# What is customize develop?

# What changed?

- Remove OpenTelemetry support
- Gauge change
- Replace Dropwizard metrics by micrometer
- Embrace functional programming
  - Immutability (final)
    - variable
    - field
    - parameter
  - declarative programming
- Remove Agent Config Update Http Handler
  - GlobalAgentHolder
  - CanaryListUpdateAgentHttpHandler
  - CanaryUpdateAgentHttpHandler
  - ConfigsUpdateAgentHttpHandler
  - PluginPropertiesHttpHandler
  - ServiceUpdateAgentHttpHandler
- Remove Agent Plugin Property Http Handler
  - PluginPropertyHttpHandler
- Remove Health check
- Remove MXBean register
- Change Constant to Enum
- Remove Config notifier