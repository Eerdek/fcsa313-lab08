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

## 2. Репозиторийн бүтэц

```
pom.xml                                         starter-ийнх (JUnit 5.10.2 + Mockito 5.11.0)
src/main/java/mn/edu/must/sqat/*.java           starter-ийнх, ӨӨРЧЛӨӨГҮЙ (мутаци бүрийн дараа сэргээсэн)
src/test/java/mn/edu/must/sqat/
    CourseRegistrationServiceTest.java          A1–A8 (7 @Test + 1 @ParameterizedTest × 3 мөр)
results/mvn-test.txt                            эцсийн ногоон гаралт — Tests run: 10, Failures: 0
results/mvn-test-mutant-1.txt                   мутант-1: идэвхгүй оюутны шалгалт комментлов
results/mvn-test-mutant-2.txt                   мутант-2: saveRegistration(...) комментлов
results/mvn-test-mutant-3.txt                   мутант-3: || courseId == null устгав (A6 засварын дараа)
results/mvn-test-mutant-3-before.txt            мутант-3: A6 засахаас ӨМНӨ — нэг ч тест унаагүй (цоорхой)
results/mvn-test-a8-strict.txt                  A8 энгийн when()-тэй — UnnecessaryStubbingException
```

## 3. Хэсэг A — Найман тест

| № | Тестийн нэр (юуг_ямарНөхцөлд_юуХүлээгдэнэ) | Гол хэрэгсэл |
|---|---|---|
| A1 | `register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail` | `when` + `verify(repository).saveRegistration(...)`, `verify(emailSender).send(eq(..), anyString(), contains("CS313"))` |
| A2 | `register_unknownStudent_returnsNoStudentAndNeitherSavesNorSends` | `verify(..., never())` |
| A3 | `register_inactiveStudent_returnsInactiveAndNeverTouchesEmail` | `verifyNoInteractions(emailSender)` |
| A4 | `register_missingPrerequisite_returnsPrerequisitesErrorAndDoesNotSave` | `when` + `never()` |
| A5 | `register_emailSenderThrows_stillReturnsOkAndRegistrationIsSaved` | `doThrow(new RuntimeException(..)).when(emailSender).send(..)` |
| A6 | `register_nullInput_throwsIllegalArgumentAndTouchesNoDependency` | `assertThrows` × 2 (studentId, courseId) + `verifyNoInteractions(repository, emailSender)` |
| A7 | `register_success_emailBodyContainsCourseCode` | `ArgumentCaptor<String>` |
| A8 | `register_errorClass_returnsErrorAndNeitherSavesNorSends` | `@ParameterizedTest` + `@CsvSource` (NO_STUDENT, INACTIVE_STUDENT, NO_COURSE) |

Matcher-ийн дүрэм: нэг дуудалтад matcher хэрэглэсэн газар бүх аргументад matcher хэрэглэсэн
(`eq(EMAIL), anyString(), contains(COURSE_ID)`); matcher-гүй дуудалтад бүх аргумент бодит утга
(`saveRegistration(STUDENT_ID, COURSE_ID)`).

**Тестийн тоо = 10** — `results/mvn-test.txt`-ийн сүүлийн мөр:
`Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` ба `BUILD SUCCESS`
(7 энгийн тест + A8-ийн 3 мөр).

### A8 ба `lenient()`

Эхлээд A8-ийг энгийн `when(...)`-ээр бичихэд NO_STUDENT ба INACTIVE мөрүүд
`UnnecessaryStubbingException`-ээр унасан (`results/mvn-test-a8-strict.txt`, Errors: 2): эдгээр
мөрөнд service оюутны шалгалт дээр буцдаг тул `courseExists` stub хэзээ ч дуудагдахгүй бөгөөд
MockitoExtension-ий strict stubs үүнийг "хэрэггүй stub" гэж үздэг. Бүх мөр нэг ижил stub-уудыг
хуваалцдаг параметржүүлсэн тестэд аль stub ашиглагдах нь мөрөөс хамаардаг тул `lenient().when(...)`
болгосон — ингэснээр бусад тестүүд strict горимоороо үлдэнэ.

## 4. Хэсэг B — Мини мутаци

Мутаци бүрийн дараа `git checkout -- src/main`-ээр кодыг сэргээсэн; эцэст нь `src/main` нь
starter-тай яг ижил (`diff -r --strip-trailing-cr` → ялгаагүй) бөгөөд `results/mvn-test.txt` ногоон.

| Мутант | Эвдрэл | Үр дүн (файл) | Унасан тест | Шалтгаан (гаралтаас) |
|---|---|---|---|---|
| 1 | `if (!student.active()) return "ERROR_INACTIVE_STUDENT";` комментлов | **Failures: 2**, BUILD FAILURE (`mvn-test-mutant-1.txt`) | A3 `register_inactiveStudent_…`; A8 `[2]` (INACTIVE мөр) | A3: `expected: <ERROR_INACTIVE_STUDENT> but was: <ERROR_NO_COURSE>`; A8[2]: `… but was: <OK>` — идэвхгүй оюутан бүртгэгдэх байсан |
| 2 | `repository.saveRegistration(studentId, courseId);` комментлов | **Failures: 2**, BUILD FAILURE (`mvn-test-mutant-2.txt`) | A1 `register_activeStudent…`; A5 `register_emailSenderThrows…` | `Wanted but not invoked: repository.saveRegistration(…)` |
| 3 | `|| courseId == null` устгав | Анх: **Failures: 0**, BUILD SUCCESS (`mvn-test-mutant-3-before.txt`) → A6 засварын дараа: **Failures: 1**, BUILD FAILURE (`mvn-test-mutant-3.txt`) | A6 `register_nullInput_…` (зөвхөн засварын дараа) | `courseId = null ==> Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.` |

Гурван мутантын аль нь ч `UnnecessaryStubbingException` (Errors) өгөөгүй — бүгд Failures.

**Мутант-3 ба A6-ийн засвар.** Анхны A6 зөвхөн `register(null, "CS313")`-ийг шалгадаг байсан тул
`courseId`-ийн null шалгалтыг устгахад нэг ч тест унаагүй — энэ бол миний тестийн цоорхой
(`mvn-test-mutant-3-before.txt`). A6-г `assertAll` дотор аргумент тус бүрийн null-ийг
(`register(null, "CS313")` ба `register("B231234567", null)`) шалгадаг болгож зассаны дараа мутант
`register(.., null)` дээр exception шидэхгүй, mock-оос `findStudent` → `null` авч `"ERROR_NO_STUDENT"`
буцаадаг болж A6 унасан. Тестийн тоо 10 хэвээр (A6 нэг метод).

**Дүгнэлт.** A7 нь амжилттай урсгалыг ажиллуулдаг ч зөвхөн и-мэйлийг шалгадаг тул мутант-2-ыг
(хадгалалт хийгдээгүй) барьсангүй — "кодыг ажиллуулсан" нь "шалгасан" гэсэн үг биш. Мутант бүрийг
дор хаяж нэг тест барьсан ч мутант-3 тестийг сайжруулах хүртэл амьд үлдсэн нь "pass болсон тест =
зөв тест" биш гэдгийг харуулав.

## 5. Дүгнэлт — mock, fake, verify

Mock бол Mockito-гийн ажиллах үед үүсгэдэг, ямар хариу өгөхийг тест бүр `when(...)`-ээр зааж,
дараа нь `verify(...)`-ээр "ямар дуудалт болсон бэ"-г шалгадаг объект бол гараар бичсэн fake нь
жинхэнэ ажилладаг хялбар хэрэгжүүлэлт (ж: `HashMap`-д хадгалдаг `InMemoryStudentRepository`) тул
дуудалтын дарааллыг биш, эцсийн төлвийг (хадгалагдсан уу) шалгахад тохиромжтой. Энэ лабд хоёр
интерфейст бодит хэрэгжүүлэлт байхгүй, мөн "и-мэйл илгээгч exception шидэх" (A5) гэх мэт fake-ээр
дуурайлгахад төвөгтэй нөхцлийг нэг мөр `doThrow`-оор үүсгэж болдог тул mock тохиромжтой байв.
Mock хийх ёсгүй зүйлс: тестлэгдэж буй класс өөрөө (`CourseRegistrationService`), `Student` шиг
энгийн өгөгдлийн объект/record (бодитоор нь `new Student(...)` үүсгэсэн), `List` зэрэг стандарт
сангийн төрлүүд, мөн өөрийн эзэмшдэггүй гадны API-г шууд — оронд нь өөрийн interface-ээр
(`EmailSender` шиг) боож, тэр interface-ээ mock хийнэ. `verify`-г хэтрүүлбэл тест хэрэгжүүлэлтийн
нарийн ширийнд (аль метод хэдэн удаа, ямар дарааллаар дуудагдсан) хэт уягдаж, үр дүн нь зөв хэвээр
байгаа refactoring (ж: хоёр дуудалтыг нэгтгэх) хийхэд л тестүүд унадаг эмзэг (brittle) болно.
Тиймээс verify-г зөвхөн гаднаас ажиглагдах, спецификацид бичигдсэн гаж нөлөөнд (хадгалсан уу,
и-мэйл явсан уу, явуулаагүй юу) хэрэглэж, бусдыг буцаах утгаар (`assertEquals`) шалгасан. Мутаци
нь энэ тэнцвэрийг баталсан: мутант-2-ыг зөвхөн `verify(repository).saveRegistration` барьсан тул
тэр verify шаардлагатай, харин дотоод дуудалт болох `courseExists`, `prerequisites`-ийг verify
хийгээгүй нь тестийг хэрэгжүүлэлтээс хамааралгүй үлдээсэн.
