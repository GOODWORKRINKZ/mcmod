# Инструкция по установке среды разработки

## Текущая ситуация:
- ✅ Gradle wrapper установлен
- ❌ Java 8 (нужна Java 17 для Minecraft 1.20.1)
- ❌ Minecraft Launcher не установлен (опционально)

## Что установить:

### 1. Java Development Kit 17 (ОБЯЗАТЕЛЬНО)

**Ссылки для загрузки:**
- Eclipse Temurin: https://adoptium.net/temurin/releases/?version=17
- Oracle JDK: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

**Установка:**
1. Скачайте .msi установщик для Windows x64
2. Запустите установку
3. ✅ Отметьте "Set JAVA_HOME variable"
4. ✅ Отметьте "Add to PATH"
5. Перезапустите PowerShell

**Проверка:**
```powershell
java -version
# Должно показать: openjdk version "17.x.x"
```

### 2. Minecraft (ОПЦИОНАЛЬНО - только для тестирования в игре)

**Для тестирования мода в игре нужно:**
- Minecraft Java Edition (купленная версия)
- Minecraft Launcher: https://www.minecraft.net/download
- Minecraft Forge 1.20.1: https://files.minecraftforge.net/

**НО**: для компиляции мода это НЕ ОБЯЗАТЕЛЬНО!
Gradle автоматически скачает нужные файлы Minecraft при сборке.

## Как собрать мод:

### После установки Java 17:

```powershell
# 1. Перейдите в папку проекта
cd D:\PROGECTS\mcmod

# 2. Первая сборка (скачает зависимости)
.\gradlew build

# 3. Готовый мод будет здесь:
# build\libs\strangerthingsmod-1.0.0.jar
```

### Если хотите протестировать в разработке:

```powershell
# Запустить тестовый клиент Minecraft (автоматически скачает всё нужное)
.\gradlew runClient

# Или запустить тестовый сервер
.\gradlew runServer
```

## Альтернатива: Использовать старую версию Minecraft

Если не хотите устанавливать Java 17, могу переделать мод под:
- **Minecraft 1.16.5** (Java 8)
- **Minecraft 1.12.2** (Java 8)

Просто скажите, и я адаптирую код под нужную версию!

## Структура собранного мода:

После сборки получите файл:
```
build/
  libs/
    strangerthingsmod-1.0.0.jar  ← Этот файл нужно поместить в папку mods
```

## Использование мода:

1. Установите Minecraft Forge для соответствующей версии
2. Скопируйте .jar файл в папку mods
3. Запустите Minecraft через Forge профиль
4. Мод будет активен!

## Команды в игре:

```
# Телепортация в Изнанку
/execute in strangerthingsmod:upside_down run tp @s ~ ~ ~

# Призвать Демогоргона
/summon strangerthingsmod:demogorgon
```
