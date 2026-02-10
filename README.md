[README.md](https://github.com/user-attachments/files/25217923/README.md)
# 🚗 Detekcja samochodów na filmach - Android

Aplikacja Android do detekcji samochodów w czasie rzeczywistym na filmach wideo z wykorzystaniem modelu YOLOv8.

## ✨ Funkcje

- 🎬 Wybór filmu z galerii lub menedżera plików
- 🔍 Analiza filmu klatka po klatce z modelem YOLOv8
- 📦 Wyświetlanie ramek wokół wykrytych samochodów podczas odtwarzania
- 🎯 Płynna interpolacja ramek między klatkami (60 FPS)
- 📊 Licznik wykrytych obiektów
- 👁️ Podgląd filmu przed przetworzeniem

## 📋 Wymagania

- Android Studio Meerkat 2024.3.2+
- JDK 11+
- Android SDK 24+
- Model YOLOv8 (plik `.tflite`)

## 🚀 Instalacja

Sklonuj repozytorium i otwórz w Android Studio:

    git clone https://github.com/sebcno0001/nazwa-repo.git

Poczekaj na synchronizację Gradle i uruchom aplikację.

## 📱 Użycie

1. Kliknij przycisk "Wybierz film"
2. Wybierz plik wideo z urządzenia
3. Kliknij "Przetwórz" — aplikacja przeanalizuje film klatka po klatce
4. Po zakończeniu analizy kliknij "Odtwórz"
5. Ramki detekcji będą wyświetlane w czasie rzeczywistym
6. Użyj "Pauza" aby zatrzymać odtwarzanie
7. Kliknij "Wyczyść" aby zresetować i wybrać nowy film

## ⚙️ Jak działa

- Film jest analizowany co 100 ms (konfigurowane przez `frameIntervalMs`)
- YOLOv8 wykrywa samochody na każdej klatce
- Podczas odtwarzania ramki są interpolowane płynnie między klatkami
- `DrawBoxes` rysuje nakładkę na `VideoView` z uwzględnieniem skali i offsetów

## 🔧 Konfiguracja

Edytuj parametry w `MainActivity.java`:

    private long frameIntervalMs = 100;  // Interwał próbkowania klatek

Dostosuj klasy detekcji w `DamageClasses.java`:

    public static final String[] CLASSES = {
        "car",
        "damage"
    };

    public static final int[] COLORS = {
        Color.RED,
        Color.YELLOW
    };

## 📁 Struktura projektu

    app/src/main/java/com/example/myapplication/
    ├── MainActivity.java          # Główna aktywność, logika UI
    ├── YoloV8.java               # Wrapper modelu YOLOv8
    ├── DrawBoxes.java            # Nakładka z ramkami detekcji
    ├── DetectionResult.java      # Klasa wyników detekcji
    ├── BoxInterpolator.java      # Interpolacja ramek
    ├── DamageClasses.java        # Mapowanie klas na kolory
    └── VideoProcessor.java       # Logika odtwarzania z nakładką

## 🛠️ Technologie

- Java, Kotlin
- Android SDK
- Gradle
- YOLOv8 (TensorFlow Lite)
- MediaMetadataRetriever (ekstrakcja klatek)
- Canvas (rysowanie ramek)

## ⚡ Optymalizacja

- Interpolacja ramek zapewnia płynne 60 FPS
- Obsługa różnych proporcji i orientacji filmów
- Automatyczne dopasowanie skali i offsetów

## 📄 Licencja

MIT License

---

👤 Autor: [sebcno0001](https://github.com/sebcno0001)
