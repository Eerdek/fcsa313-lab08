package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final String STUDENT_ID = "B231234567";
    private static final String EMAIL = "b231234567@must.edu.mn";
    private static final String COURSE_ID = "CS313";

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private static Student student(boolean active, List<String> coursesTaken) {
        return new Student(STUDENT_ID, EMAIL, active, coursesTaken);
    }

    // A1 — when + verify
    @Test
    void register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail() {
        // Arrange
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of("CS201")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));
        // Act
        String result = service.register(STUDENT_ID, COURSE_ID);
        // Assert
        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), contains(COURSE_ID));
    }

    // A2 — verify(..., never())
    @Test
    void register_unknownStudent_returnsNoStudentAndNeitherSavesNorSends() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3 — verifyNoInteractions
    @Test
    void register_inactiveStudent_returnsInactiveAndNeverTouchesEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(false, List.of("CS201")));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A4 — when + never
    @Test
    void register_missingPrerequisite_returnsPrerequisitesErrorAndDoesNotSave() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of()));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A5 — doThrow(...).when(...)
    @Test
    void register_emailSenderThrows_stillReturnsOkAndRegistrationIsSaved() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of("CS201")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));
        doThrow(new RuntimeException("SMTP сервер унтарсан"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), anyString());
    }

    // A6 — assertThrows
    @Test
    void register_nullInput_throwsIllegalArgumentAndTouchesNoDependency() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, COURSE_ID));

        verifyNoInteractions(repository, emailSender);
    }

    // A7 — ArgumentCaptor
    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(student(true, List.of("CS201")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS201"));
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

        service.register(STUDENT_ID, COURSE_ID);

        verify(emailSender).send(eq(EMAIL), eq("Бүртгэл баталгаажлаа"), body.capture());
        assertTrue(body.getValue().contains(COURSE_ID),
                "И-мэйлийн биед хичээлийн код байх ёстой: " + body.getValue());
    }

    // A8 — @ParameterizedTest + @CsvSource
    @ParameterizedTest(name = "[{index}] studentExists={0}, active={1}, courseExists={2} -> {3}")
    @CsvSource({
            "false, false, true,  ERROR_NO_STUDENT",
            "true,  false, true,  ERROR_INACTIVE_STUDENT",
            "true,  true,  false, ERROR_NO_COURSE"
    })
    void register_errorClass_returnsErrorAndNeitherSavesNorSends(
            boolean studentExists, boolean active, boolean courseExists, String expected) {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(studentExists ? student(active, List.of("CS201")) : null);
        when(repository.courseExists(COURSE_ID)).thenReturn(courseExists);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals(expected, result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}
