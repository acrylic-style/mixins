# mixins
mixin that doesn't require `agentmain` or `premain`

## Usage
See `src/test/java/net/azisaba/mixins/test/MixinTest` for example usage.

## Features

### Implemented
- `@Inject` (at = HEAD, TAIL), `@MixinName`, `@ConstructorCall`, `@Mixin` (value, target)

### Not implemented
- `@DontOverride`, `@Shadow`
