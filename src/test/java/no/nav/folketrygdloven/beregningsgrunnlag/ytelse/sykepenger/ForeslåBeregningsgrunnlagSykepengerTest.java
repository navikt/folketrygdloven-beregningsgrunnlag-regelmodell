package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.sykepenger;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

public class ForeslåBeregningsgrunnlagSykepengerTest {

    private LocalDate skjæringstidspunkt;

    @BeforeEach
    void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    }

    @Test
    void skalBeregneSykepengerGrunnlagMedInntektsmeldingMedNaturalYtelserIArbeidsgiverperioden() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusDays(7);
        List<Periode> arbeidsgiversPeriode = List.of(Periode.of(skjæringstidspunkt, skjæringstidspunkt.plusDays(14)));
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        Beregningsgrunnlag.builder(beregningsgrunnlag).medBeregningForSykepenger(true);
        opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(42000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold).medArbeidsgiverperioder(arbeidsgiversPeriode).build();
        // Act
	    RegelResultat resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
        // Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).isEmpty();
        assertThat(bgArbeidsforhold.getNaturalytelseBortfaltPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(24000)); //NOSONAR
        assertBeregningsgrunnlag(grunnlag, månedsinntekt, 24000);
    }

    @Test
    void skalBeregneSykepengerGrunnlagMedInntektsmeldingMedNaturalYtelserUtenforArbeidsgiverperioden() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusDays(7);
        List<Periode> arbeidsgiversPeriode = List.of(Periode.of(skjæringstidspunkt, skjæringstidspunkt.plusDays(6)),
            Periode.of(skjæringstidspunkt.plusDays(10), skjæringstidspunkt.plusDays(18)));
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        Beregningsgrunnlag.builder(beregningsgrunnlag).medBeregningForSykepenger(true);
        opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(40000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold).medArbeidsgiverperioder(arbeidsgiversPeriode).build();
        // Act
	    RegelResultat resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
        // Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).isEmpty();
        assertThat(bgArbeidsforhold.getNaturalytelseBortfaltPrÅr()).isEmpty();
        assertBeregningsgrunnlag(grunnlag, månedsinntekt, 0);
    }

    @Test
    void skalIkkeBeregneSykepengerGrunnlagMedInntektsmeldingMedNaturalYtelserIArbeidsgiverperiodenNårIFPSAK() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusDays(7);
        List<Periode> arbeidsgiversPeriode = List.of(Periode.of(skjæringstidspunkt, skjæringstidspunkt.plusDays(14)));
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt,
            refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(40000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold).medArbeidsgiverperioder(arbeidsgiversPeriode).build();
        // Act
        new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
        // Assert
        assertThat(bgArbeidsforhold.getNaturalytelseBortfaltPrÅr()).isEmpty();
        assertBeregningsgrunnlag(grunnlag, månedsinntekt, 0);
    }

    private void assertBeregningsgrunnlag(BeregningsgrunnlagPeriode grunnlag, BigDecimal månedsinntekt, int naturalYtelsePrÅr) {
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(naturalYtelsePrÅr));
        assertThat(grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL).getAvvikPromille()).isEqualTo(0);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }
}

