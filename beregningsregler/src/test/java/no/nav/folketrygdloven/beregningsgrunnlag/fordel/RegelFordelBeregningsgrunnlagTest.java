package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

class RegelFordelBeregningsgrunnlagTest {

    private static final String ORGNR1 = "995";
    private static final String ORGNR2 = "910";
    private static final String ORGNR3 = "973";

    @Test
    void skal_flytte_beregningsgrunnlag_fra_SN_til_to_arbeidsforhold_uten_rest() {
        // Arrange
        var refusjonskrav1 = BigDecimal.valueOf(200_000);
	    var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
	    var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

	    var refusjonskrav2 = BigDecimal.valueOf(150_000);
	    var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
	    var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

        var beregnetPrÅrSN = BigDecimal.valueOf(150_000);
	    var sn = lagSN(beregnetPrÅrSN);

	    var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), new ArrayList<>(Arrays.asList(a1, a2, sn)));

        // Act
        kjørRegel(periode);

        // Assert
        var flyttetFraNæringArbeid1 = periode.getAlleAndelerForStatus(AktivitetStatus.AT).stream()
            .filter(a -> a.getArbeidsforhold().equals(a1.getArbeidsforhold()) && a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE))
            .findFirst().orElseThrow();
        assertThat(flyttetFraNæringArbeid1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(a1.getBruttoPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));

        var flyttetFraNæringArbeid2 = periode.getAlleAndelerForStatus(AktivitetStatus.AT).stream()
            .filter(a -> a.getArbeidsforhold().equals(a2.getArbeidsforhold()) && a.getInntektskategori().equals(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE))
            .findFirst().orElseThrow();
        assertThat(flyttetFraNæringArbeid2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(a2.getBruttoPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(100_000));

        assertThat(sn.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
    }


	@Test
	void skal_flytte_beregningsgrunnlag_fra_ett_arbeidsforhold_til_to_andre_arbeidsforhold_uten_rest() {
		// Arrange
		var refusjonskrav1 = BigDecimal.valueOf(200_000);
		var beregnetPrÅr1 = BigDecimal.valueOf(100_000);
		var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

		var refusjonskrav2 = BigDecimal.valueOf(150_000);
		var beregnetPrÅr2 = BigDecimal.valueOf(100_000);
		var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

		var refusjonskrav3 = BigDecimal.ZERO;
		var beregnetPrÅr3 = BigDecimal.valueOf(150_000);
		var a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3);

		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(a1, a2, a3));

		// Act
		kjørRegel(periode);

		// Assert
		assertThat(a1.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskrav1);
		assertThat(a2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(refusjonskrav2);
		assertThat(a3.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
	}

    @Test
    void skal_ikkje_omfordele() {
        // Arrange
        var refusjonskrav1 = BigDecimal.valueOf(200_000);
	    var beregnetPrÅr1 = BigDecimal.valueOf(200_000);
	    var a1 = lagArbeidsforhold(refusjonskrav1, beregnetPrÅr1, 1L, ORGNR1);

	    var refusjonskrav2 = BigDecimal.valueOf(150_000);
	    var beregnetPrÅr2 = BigDecimal.valueOf(150_000);
	    var a2 = lagArbeidsforhold(refusjonskrav2, beregnetPrÅr2, 2L, ORGNR2);

	    var refusjonskrav3 = BigDecimal.ZERO;
	    var beregnetPrÅr3 = BigDecimal.valueOf(150_000);
	    var a3 = lagArbeidsforhold(refusjonskrav3, beregnetPrÅr3, 3L, ORGNR3);

	    var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(a1, a2, a3));

        // Act
        kjørRegel(periode);

        // Assert
        assertThat(a1.getFordeltPrÅr()).isEmpty();
        assertThat(a2.getFordeltPrÅr()).isEmpty();
        assertThat(a3.getFordeltPrÅr()).isEmpty();
    }


    private void kjørRegel(FordelPeriodeModell periode) {
        new RegelFordelBeregningsgrunnlag().evaluerRegel(periode, new ArrayList<>());
    }

    private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr, Long andelsnr, String orgnr) {
		return lagArbeidsforhold(refusjonskravPrÅr, beregnetPrÅr, andelsnr, orgnr, null);
    }

	private FordelAndelModell lagArbeidsforhold(BigDecimal refusjonskravPrÅr, BigDecimal beregnetPrÅr, Long andelsnr, String orgnr, BigDecimal inntektFraIM) {
		return FordelAndelModell.builder()
				.medAndelNr(andelsnr)
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, null))
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER)
				.medAktivitetStatus(AktivitetStatus.AT)
				.medGjeldendeRefusjonPrÅr(refusjonskravPrÅr)
				.medForeslåttPrÅr(beregnetPrÅr)
				.medInntektFraInnektsmelding(inntektFraIM)
				.build();
	}

    private FordelAndelModell lagSN(BigDecimal beregnetPrÅr1) {
        return FordelAndelModell.builder()
            .medAndelNr(2L)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SN)
            .medForeslåttPrÅr(beregnetPrÅr1).build();
    }


}
