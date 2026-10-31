# Лаборатори №8 — Mock объектууд: Mockito-гоор хамаарлыг орлуулж тестлэх

**Оюутан:** Эрдэнэбаяр
**Оюутны код:** B232270136
**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)

---

## 1. Ажлын орчин

```
$ java -version
openjdk version "17.0.20.1" 2026-08-18
OpenJDK Runtime Environment Temurin-17.0.20.1+1 (build 17.0.20.1+1)
OpenJDK 64-Bit Server VM Temurin-17.0.20.1+1 (build 17.0.20.1+1, mixed mode, sharing)

$ mvn -version
Apache Maven 3.9.9 (8e8579a9e76f7d015ee5ec7bfcdc97d260186937)
Maven home: C:\Users\THE_FEDzUP\tools\apache-maven-3.9.9
Java version: 17.0.20.1, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

| Зүйл | Утга |
|---|---|
| JDK | Eclipse Temurin OpenJDK 17.0.20.1 (JDK, `javac`-тэй) |
| Build | Apache Maven 3.9.9 |
| Тест | JUnit Jupiter 5.10.2 + Mockito 5.11.0 (`mockito-junit-jupiter`), surefire 3.2.5 |
| ҮС | Windows 11 Pro (26200), Git Bash |

> Starter төслийг (Teams → Class Materials → Labs → Lab08) өөрчлөлгүй хуулсан: `pom.xml`,
> `src/main/java/mn/edu/must/sqat/` (`Student`, `StudentRepository`, `EmailSender`,
> `CourseRegistrationService`). Анхны `mvn test` → "No tests to run." + BUILD SUCCESS.
