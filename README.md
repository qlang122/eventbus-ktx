# ktx Eventbus

from：[https://github.com/fengzhizi715/EventBus](https://github.com/fengzhizi715/EventBus)

## USE
```
dependencies {
	implementation 'com.github.qlang122:eventbus-ktx:1.0.0'
}
```

1.simple event.
```kotlin
EventBus.register("Tag-of-name", XXXEvent::class.java,{
// event back.
})
```

2.sticky event.
```kotlin
EventBus.registerSticky("Tag-of-name", XXXEvent::class.java,{
// event back.
})
```

3.send events.
```kotlin
EventBus.post(XXXEvent())
//EventBus.postSticky(XXXEvent()) //sticky event
```

4.remove register.
```kotlin
EventBus.unregister("Tag-of-name")
```
