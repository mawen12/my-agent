
# What is customize develop?

for learning.

# What changed?

## Remove OpenTelemetry support

Remove Dependency.
Remove Code.

## Gauge change && Replace Dropwizard metrics by micrometer

Use Micrometer to implement metric.

## Embrace functional programming

- Immutability (final)
  - variable
  - field
  - parameter
- declarative programming

## Remove Agent Config Update Http Handler

- GlobalAgentHolder
- CanaryListUpdateAgentHttpHandler
- CanaryUpdateAgentHttpHandler
- ConfigsUpdateAgentHttpHandler
- PluginPropertiesHttpHandler
- ServiceUpdateAgentHttpHandler

## Remove Agent Plugin Property Http Handler

- PluginPropertyHttpHandler

## Remove Health check

## Remove MXBean register

## Change Constant to Enum

## Remove Config notifier

## Remove Agent Info

- AgentInfoProvider
- AgentInfoFactory

## Remove Plugin AutoRefresh

## Create Fluent Interfaces Using Lambda Expressions

- Method Chaining
- Like Stream contains non-terminal and terminal action
- Make the API Intuitive and Fluent
- Protect instance, manage object lifetime


## Remove Custom HttpServer