package no.nav.folketrygdloven.kalkulus.felles.v1;

import java.util.concurrent.atomic.AtomicLong;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktørIdPersonident extends PersonIdent {
    public static final String IDENT_TYPE = "AKTØRID";
    private static final AtomicLong DUMMY_AKTØRID = new AtomicLong(1000000000000L);

    @JsonProperty(value = "ident", required = true, index = 1)
    @NotNull
    @Pattern(regexp = "^\\d{13}+$", message = "aktørId ${validatedValue} har ikke gyldig verdi (13 siffer)")
    private String ident;

    @JsonCreator
    public AktørIdPersonident(@JsonProperty(value = "ident", required=true, index=1) String ident) {
        this.ident = ident;
    }

    @Override
    public String getIdent() {
        return ident;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }


    /** Genererer dummy aktørid unikt for test. */
    public static AktørIdPersonident dummy( ) {
        return new AktørIdPersonident(String.valueOf(DUMMY_AKTØRID.getAndIncrement()));
    }
}
