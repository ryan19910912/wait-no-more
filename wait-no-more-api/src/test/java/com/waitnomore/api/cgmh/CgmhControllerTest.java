package com.waitnomore.api.cgmh;
import com.waitnomore.api.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class CgmhControllerTest {
    @Test void rejectsInvalidInputsWithoutContactingSource() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new CgmhController(new CgmhService()))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/api/hospitals/cgmh/branches")).andExpect(status().isOk());
        mvc.perform(get("/api/hospitals/cgmh/invalid/progress?department=02&session=1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/hospitals/cgmh/1/progress")).andExpect(status().isBadRequest());
    }
}
