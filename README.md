<h1 align="center">Pumpk1n</h1>
<p align="center">
    <img src="http://ForTheBadge.com/images/badges/made-with-java.svg" alt="Made with Java"><br>
    <img src="https://forthebadge.com/images/badges/powered-by-water.svg" alt="Powered by Water"><br>
    <img src="https://forthebadge.com/images/badges/you-didnt-ask-for-this.svg" alt="Powered by Water">
    <br>
    <img src="https://img.shields.io/github/license/lilmayu/Pumpk1n.svg" alt="License">
    <img src="https://img.shields.io/github/v/release/lilmayu/Pumpk1n.svg" alt="Version">
</p>
<p align="center">
    Pumpk1n - Lightweight and customizable tool for quick data management using JSON serialization.
    <br>
    Made by <a href="https://mayuna.dev">Mayuna</a>
</p>

## Installation
### Maven
```xml
<dependency>
    <groupId>dev.mayuna</groupId>
    <artifactId>pumpk1n</artifactId>
    <version>VERSION</version>
</dependency>
<!-- + GSON and possibly MySQL with HikaryCP or SQLite -->
```
### Gradle
```gradle
repositories {
    mavenCentral()
}

dependencies {
    // Change 'implementation' to 'compile' on old Gradle versions
    implementation 'dev.mayuna:pumpk1n:VERSION'

    // You will also need...
    implementation 'com.google.code.gson:gson:2.9.0'

    /// And if you will be using SQLite / SQL default Storage Handler implementation...
    // SQLite
    compileOnly 'org.xerial:sqlite-jdbc:3.36.0.3'

    // MySQL
    compileOnly 'mysql:mysql-connector-java:8.0.29'
    compileOnly 'com.zaxxer:HikariCP:3.4.5' // HikariCP - Pro TIP: You should use newer version!
}
```
- Replace `VERSION` with version number without `v` character
- For version number see latest [Maven Repository](https://mvnrepository.com/artifact/dev.mayuna/pumpk1n) release (should be same with Github Release though)
- You can also use [GitHub Releases](https://github.com/lilmayu/pumpk1n/releases)

## Requirements
- Java >= 8
- [GSON](https://github.com/google/gson) 
- And possibly other libraries (see Gradle installation)

## Documentation
An example is worth a thousand words.
```java
/// Loading
Pumpk1n pumpk1n = new Pumpk1n(new FolderStorageHandler("./data/"));
pumpk1n.prepareStorage();

UUID uuid = UUID.randomUUID();

DataHolder dataHolder = pumpk1n.getOrCreateDataHolder(uuid);
TestData testData = dataHolder.getOrCreateDataElement(TestData.class);

// -> Boom, instance of TestData

/// Saving
pumpk1n.save(dataHolder); 
// OR
dataHolder.save();
```
### Thousand words
First, you need to create `Pumpk1n` object. In its constructor, you pass implementation of `StorageHandler`.

There are multiple pre-implemented `StorageHandler`s:
- `FolderStorageHandler` - Simple. Uses local files as a data storage.
- `SQLiteStorageHandler` - Uses SQLite database as a data storage.
- `SQLStorageHandler` - Uses SQL database as a data storage.

```java
// Creating FolderStorageHandler

FolderStorageHandler storageHandler = new FolderStorageHandler("/path/to/folder");
```
```java
// Creating SQLiteStorageHandler

SQLiteStorageHandler storageHandler = new SQLiteStorageHandler(); // Default settings
// OR
SQLiteSvtorageHandler storageHandler = new SQLiteStorageHandler(
    SQLiteStorageHandler.Settings.Builder.create()
        .setFileName("database.db") // Database file
        .setTableName("pumpkin") // Table name which will be used
    .build()
);
```
```java
// Creating SQLStorageHandler

// Pro TIP: These values are used only for an example, you should tweak them to your liking.
HikariConfig hikariConfig = new HikariConfig();
hikariConfig.setJdbcUrl("jdbc:mysql://hostname/database");
hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
hikariConfig.setUsername("username");
hikariConfig.setPassword("password");
hikariConfig.setMinimumIdle(2);
hikariConfig.setMaximumPoolSize(5);
hikariConfig.setConnectionTimeout(30000);

String tableName = "pumpkin"; // Table name which will be used

SQLStorageHandler sQLStorageHandler = new SQLStorageHandler(hikariConfig, tableName);
```

After creating `Pumpk1n` object, you can start using it! There are few methods...
### `Pumpk1n`'s methods
| Method | Returns | Arguments | Description |
|--------|---------|-----------|-------------|
|`#prepareStorage()`|void|none|Prepares storage|
|`#getDataHolder()`|`DataHolder` (nullable)|`UUID`|Returns loaded `DataHolder`|
|`#getOrLoadDataHolder()`|`DataHolder` (nullable)|`UUID`|Returns `DataHolder` if exists, otherwise loads `DataHolder` from storage.|
|`#getOrCreateDataHolder()`|`DataHolder`|`UUID`|Same as `#getOrLoadDataHolder()`, however, if it could not load specified `DataHolder`, it creates and loads new `DataHolder` (without saving it to the storage)|
|`#addToMemoryDataHolder()`|void|`DataHolder`|Adds specified `DataHolder` into memory if there's no `DataHolder` with its UUID|
|`#unloadDataHolder()`|boolean|`UUID`|Unloads `DataHolder` from memory|
|`#deleteDataHolder()`|boolean|`UUID`|Unloads `DataHolder` and removes it from storage|
|`#saveDataHolder()`|void|`DataHolder`|Saves `DataHolder` to storage|

After getting your `DataHolder`, you can also start using it! (wow) There are few methods...
### `DataHolder`'s methods
| Method | Returns | Arguments | Description |
|--------|---------|-----------|-------------|
|`#loadFromJsonObject()`|`DataHolder`|`Pumpk1n`, `JsonObject`|(**static method**) Parses `JsonObject` into `DataHolder`|
|`#getAsJsonObject()`|`JsonObject`|none|Converts `DataHolder` into `JsonObject`|
|`#getOrCreateDataElement()`|`T`|`Class<T>`|Gets or creates specified `DataElement` - Your implementation of `DataElement` **must have** public no-args constructor|
|`#getDataElement()`|`T` (nullable)|`Class<T>`|Same as `#getOrCreateDataElement()` but without the creating part|
|`#removeDataElement()`|boolean|`Class<T>`|Removes your implementation of `DataElement` from memory|
|`#delete()`|boolean|none|Deletes current `DataHolder` from storage (same as `Pumpk1n#deleteDataHolder()`)|
|`#save()`|boolean|none|Saves current `DataHolder` to storage (same as `Pumpk1n#saveDataHolder()`)|

As you can see, I've mentioned `DataElement` - Using this class, you extend (implement) any class that you want to ack like "data-class" or something like that. **It must have public no-args constructor** or there will be exceptions. Here's example implementation of `DataElement`
```java
public class TestData implements DataElement {

    // Some fiels which will be (de)serialized by Gson...
    public int someNumber = 69;

    // Optional part!
    @Override
    public GsonBuilder getGsonBuilder() {
        // Here, you can override default GsonBuilder by your own.
        // Useful if you use @Expose annotations for example.

        return new GsonBuilder();
    }
}
```

#### Custom `StorageHandler` implementation
You can easily create your own `StorageHandler` implementation! Just extend any class with `StorageHandler`, override required methods and you are good to go. Oh, and you will have to give it a name. Current pre-implemented storage handlers have their class name as a name.
```java
public class CustomStorageHandler extends StorageHandler {

    public CustomStorageHandler() {
        super(CustomStorageHandler.class.getSimpleName()); // Or any other...
    }

    @Override
    public void prepareStorage() {
        // Prepares storage.
    }

    @Override
    public void saveHolder(DataHolder dataHolder) {
        // Saves DataHolder into storage.
    }

    @Override
    public DataHolder loadHolder(UUID uuid) {
        // Loads DataHolder from storage.
    }

    @Override
    public boolean removeHolder(UUID uuid) {
        // Removes DataHolder from storage.
    }
}
```
You should have a look on [pre-implemented storage handlers](https://github.com/lilmayu/Pumpk1n/tree/main/src/main/java/dev/mayuna/pumpk1n/impl) before creating your own. I recommend looking into [FolderStorageHandler](https://github.com/lilmayu/Pumpk1n/blob/main/src/main/java/dev/mayuna/pumpk1n/impl/FolderStorageHandler.java) since it is very simple.

Oh, and another pro tip: `StorageHandler` has a `Pumpk1n` object inside itself. You have to pass this object into `DataHolder#loadFromJsonObject()` [(example)](https://github.com/lilmayu/Pumpk1n/blob/aa1b8fca4c1799c2ea80975db5cf2fdbf98d83f0/src/main/java/dev/mayuna/pumpk1n/impl/FolderStorageHandler.java#L63).

## Why?
I don't know. I've created this system in my already existing project and I've noticed that it is pretty quick'n'easy to use so I've made library implementation of this, which can be easily extended by your own implementation.

## Lastly...
If you have any questions, feel free to create an issue, I'll gladly respond to you. Also, if you've used this library and found it useful, consider [supporting me ❤️](https://github.com/sponsors/lilmayu), it would mean the world to me.
