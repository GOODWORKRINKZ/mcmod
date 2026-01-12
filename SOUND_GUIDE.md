# Звуки Демогоргона

## Установленные звуки

Мод теперь включает настоящие звуки Демогоргона из сериала "Очень странные дела"!

### Звуковые файлы

Расположение: `src/main/resources/assets/strangerthingsmod/sounds/`

- **demogorgon_ambient.ogg** - низкое рычание/фоновый звук
- **demogorgon_scream.ogg** - пугающий крик

### Зарегистрированные звуковые события

В `ModSounds.java`:

1. `DEMOGORGON_AMBIENT` - фоновый звук, проигрывается когда демогоргон рядом
2. `DEMOGORGON_SCREAM` - крик демогоргона  
3. `DEMOGORGON_HURT` - звук когда демогоргон получает урон (использует scream с пониженной громкостью)
4. `DEMOGORGON_DEATH` - звук смерти демогоргона (использует scream с измененной высотой тона)

### Где звуки проигрываются

**В DemogorgonEntity.java:**

- `getAmbientSound()` - ambient звук проигрывается периодически (каждые ~80 тиков)
- `getHurtSound()` - проигрывается когда демогоргон получает урон
- `getDeathSound()` - проигрывается при смерти
- `doHurtTarget()` - 33% шанс проиграть scream при каждой атаке

### Настройка звуков в sounds.json

```json
{
  "entity.demogorgon.ambient": {
    "subtitle": "strangerthingsmod.subtitle.demogorgon.ambient",
    "sounds": [
      {
        "name": "strangerthingsmod:demogorgon_ambient",
        "stream": false
      }
    ]
  }
}
```

### Переводы субтитров

**Английский (en_us.json):**
- "Demogorgon growls" - рычание
- "Demogorgon screams" - крик
- "Demogorgon hurts" - ранен
- "Demogorgon dies" - умирает

**Русский (ru_ru.json):**
- "Демогоргон рычит"
- "Демогоргон кричит"  
- "Демогоргон ранен"
- "Демогоргон умирает"

## Как добавить новые звуки

1. Конвертировать аудио файл в OGG Vorbis формат:
   ```bash
   ffmpeg -i input.mp3 -c:a libvorbis -q:a 4 output.ogg
   ```

2. Поместить .ogg файл в `src/main/resources/assets/strangerthingsmod/sounds/`

3. Зарегистрировать в `ModSounds.java`:
   ```java
   public static final RegistryObject<SoundEvent> NEW_SOUND =
       registerSoundEvent("entity.demogorgon.new_sound");
   ```

4. Добавить в `sounds.json`:
   ```json
   "entity.demogorgon.new_sound": {
     "subtitle": "strangerthingsmod.subtitle.demogorgon.new_sound",
     "sounds": ["strangerthingsmod:sound_file_name"]
   }
   ```

5. Добавить переводы в языковые файлы

6. Использовать в коде:
   ```java
   this.playSound(ModSounds.NEW_SOUND.get(), 1.0F, 1.0F);
   ```

## Источники звуков

Звуки взяты из:
- https://www.myinstants.com/media/sounds/demogorgon-classic-sound-effect.mp3
- https://www.myinstants.com/media/sounds/demogorgon-scream.mp3
