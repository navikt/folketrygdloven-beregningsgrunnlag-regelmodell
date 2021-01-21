package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2019;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;

class RegelFullføreBeregningsgrunnlagFRISINNTest {
    public static final String ORGNR = "14263547852";
    public static final BigDecimal SEKS_G = BigDecimal.valueOf(GRUNNBELØP_2019 * 6);
    private static final BigDecimal DEKNINGSGRAD_80 = BigDecimal.valueOf(0.8);

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);


    @Test
    public void kun_sn_under_6G_inntekt_ingen_avkorting_100_prosent_utbetaling() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, null, null, 100d, 0d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), BigDecimal.valueOf(100_000), BigDecimal.valueOf(80_000));
    }
    @Test
    public void kun_sn_under_6G_inntekt_ingen_avkorting_50_prosent_utbetaling() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, null, null, 50d, 0d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), BigDecimal.valueOf(50_000), BigDecimal.valueOf(40_000));
    }
    @Test
    public void kun_sn_over_6G_inntekt_ingen_avkorting_100_prosent_utbetaling() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(800_000d, null, null, 100d, 0d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        BigDecimal avkortet = SEKS_G;
        BigDecimal redusert = avkortet.multiply(DEKNINGSGRAD_80);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
    }

    @Test
    public void kun_sn_over_6G_inntekt_ingen_avkorting_50_prosent_utbetaling() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(800_000d, null, null, 50d, 0d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        BigDecimal avkortet = SEKS_G.subtract(BigDecimal.valueOf(400_000));
        BigDecimal redusert = avkortet.multiply(DEKNINGSGRAD_80);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
    }

    @Test
    public void fl_sn_over_under_6G_inntekt_ingen_avkorting_100_prosent_utbetaling() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, null, 100d, 100d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        assertThat(regelResultat).isNotNull();
        BigDecimal avkortet = BigDecimal.valueOf(200_000);
        BigDecimal redusert = avkortet.multiply(DEKNINGSGRAD_80);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortet, redusert);
    }

    @Test
    public void fl_sn_over_over_6G_med_ingen_avkorting_100_prosent_utbetaling() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(400_000d, 400_000d, null, 100d, 100d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal avkortetFL = BigDecimal.valueOf(400_000);
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal avkortetSN = SEKS_G.subtract(avkortetFL);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
    }

    @Test
    public void fl_sn_over_over_6G_med_ingen_avkorting_delvis_utbetaling_fl_full_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(400_000d, 400_000d, null, 100d, 50d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal avkortetFL = BigDecimal.valueOf(200_000);
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal løpendeFL = BigDecimal.valueOf(200_000);
        BigDecimal avkortetSN = SEKS_G.subtract(avkortetFL).subtract(løpendeFL);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    public void at_sn_under_6G_med_ingen_avkorting_full_utb_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(300_000d, null, 200_000d, 100d, null);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal avkortetSN = BigDecimal.valueOf(300_000);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    public void at_sn_over_6G_med_avkorting_delvis_utb_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(600_000d, null, 500_000d, 50d, null);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal avkortetSN = BigDecimal.ZERO;
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    public void at_sn_over_6G_med_avkorting_full_utb_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(600_000d, null, 500_000d, 100d, null);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal bruttoAT = BigDecimal.valueOf(500_000);
        BigDecimal avkortetSN = SEKS_G.subtract(bruttoAT);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    public void at_sn_under_6G_uten_avkorting_delvis_utb_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, null, 400_000d, 50d, null);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal avkortetSN = BigDecimal.valueOf(100_000).divide(BigDecimal.valueOf(2), RoundingMode.HALF_EVEN);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
    }

    @Test
    public void at_fl_sn_under_6G_uten_avkorting_full_utb_sn_og_fl() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, 50_000d, 200_000d, 100d, 100d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal avkortetFL = BigDecimal.valueOf(50_000);
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal avkortetSN = BigDecimal.valueOf(100_000);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    public void at_fl_sn_under_6G_uten_avkorting_delvis_utb_fl() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(100_000d, 50_000d, 200_000d, 100d, 50d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal avkortetFL = BigDecimal.valueOf(25_000);
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal avkortetSN = BigDecimal.valueOf(100_000);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    public void at_fl_sn_over_6G_med_avkorting_av_sn_full_utb_fl_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, 400_000d, 100d, 100d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal bruttoAT = BigDecimal.valueOf(400_000);
        BigDecimal avkortetFL = SEKS_G.subtract(bruttoAT);
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).max(BigDecimal.ZERO);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    public void at_fl_sn_over_6G_uten_avkorting_delvis_utb_fl_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, 400_000d, 100d, 10d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal bruttoAT = BigDecimal.valueOf(400_000);
        BigDecimal løpedeFL = BigDecimal.valueOf(180_000);
        BigDecimal avkortetFL = SEKS_G.subtract(bruttoAT).subtract(løpedeFL);
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).subtract(løpedeFL).max(BigDecimal.ZERO);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    public void at_fl_sn_over_6G_uten_avkorting_ingen_utb_fl() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 200_000d, 400_000d, 100d, 0d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal bruttoAT = BigDecimal.valueOf(400_000);
        BigDecimal løpedeFL = BigDecimal.valueOf(200_000);
        BigDecimal avkortetFL = SEKS_G.subtract(bruttoAT).subtract(løpedeFL).max(BigDecimal.ZERO);
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).subtract(løpedeFL).max(BigDecimal.ZERO);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }

    @Test
    public void at_fl_sn_over_6G_uten_avkorting_ingen_utb_fl_utb_sn() {
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(200_000d, 100_000d, 400_000d, 100d, 0d);
        BeregningsgrunnlagPeriode bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        RegelResultat regelResultat = kjørRegel(bgPeriode);

        BigDecimal bruttoAT = BigDecimal.valueOf(400_000);
        BigDecimal løpedeFL = BigDecimal.valueOf(100_000);
        BigDecimal avkortetFL = BigDecimal.ZERO;
        BigDecimal redusertFL = avkortetFL.multiply(DEKNINGSGRAD_80);
        BigDecimal avkortetSN = SEKS_G.subtract(bruttoAT).subtract(avkortetFL).subtract(løpedeFL).max(BigDecimal.ZERO);
        BigDecimal redusertSN = avkortetSN.multiply(DEKNINGSGRAD_80);
        assertThat(regelResultat).isNotNull();
        assertFLAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetFL, redusertFL);
        assertSNAndel(bgPeriode.getBeregningsgrunnlagPrStatus(), avkortetSN, redusertSN);
        assertIngenUtbetalingAT(bgPeriode.getBeregningsgrunnlagPrStatus());
    }


    private void assertIngenUtbetalingAT(Collection<BeregningsgrunnlagPrStatus> andeler) {
        BeregningsgrunnlagPrStatus atflAndel = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.ATFL)).findFirst().orElse(null);
        assertThat(atflAndel).isNotNull();
        boolean finnesArbforMedUtbetaling = atflAndel.getArbeidsforholdIkkeFrilans().stream().anyMatch(a -> a.getRedusertPrÅr().compareTo(BigDecimal.ZERO) != 0);
        assertThat(finnesArbforMedUtbetaling).isFalse();
    }


    private void assertFLAndel(Collection<BeregningsgrunnlagPrStatus> andeler, BigDecimal avkortet, BigDecimal redusert) {
        BeregningsgrunnlagPrStatus atflAndel = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.ATFL)).findFirst().orElse(null);
        assertThat(atflAndel).isNotNull();
        BeregningsgrunnlagPrArbeidsforhold flAndel = atflAndel.getFrilansArbeidsforhold().orElse(null);
        assertThat(flAndel).isNotNull();
        assertThat(flAndel.getAvkortetPrÅr()).isEqualByComparingTo(avkortet);
        assertThat(flAndel.getRedusertPrÅr()).isEqualByComparingTo(redusert);
    }

    private void assertSNAndel(Collection<BeregningsgrunnlagPrStatus> andeler, BigDecimal avkortet, BigDecimal redusert) {
        BeregningsgrunnlagPrStatus snAndel = andeler.stream().filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.SN)).findFirst().orElse(null);
        assertThat(snAndel).isNotNull();
        assertThat(snAndel.getAvkortetPrÅr()).isEqualByComparingTo(avkortet);
        assertThat(snAndel.getRedusertPrÅr()).isEqualByComparingTo(redusert);
    }

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
        RegelFinnGrenseverdiFRISINN grenseregel = new RegelFinnGrenseverdiFRISINN(grunnlag);
        RegelFullføreBeregningsgrunnlagFRISINN fastsettregel = new RegelFullføreBeregningsgrunnlagFRISINN(grunnlag);
        grenseregel.evaluer(grunnlag);
        Evaluation evaluation = fastsettregel.evaluer(grunnlag);
        return RegelmodellOversetter.getRegelResultat(evaluation, "input");
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Double snInntektPrÅr, Double frilansInntektPrÅr, Double arbeidsinntektPrÅr, Double snUtbetalingsgrad, Double flUtbetalingsgrad) {
        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(new Periode(skjæringstidspunkt, null));
        byggSN(snInntektPrÅr, periodeBuilder, snUtbetalingsgrad);
        byggATFL(frilansInntektPrÅr, arbeidsinntektPrÅr, periodeBuilder, flUtbetalingsgrad);
        BeregningsgrunnlagPeriode periode = periodeBuilder.build();
        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medAntallGMinstekravVilkår(BigDecimal.valueOf(0.75))
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL_SN, null)))
            .medBeregningsgrunnlagPeriode(periode)
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
    }

    private void byggSN(Double snInntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder, Double utbetalingsgrad) {
        if (snInntektPrÅr != null) {
            periodeBuilder.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.SN)
                .medAndelNr(1L)
                .medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
                .medBeregnetPrÅr(BigDecimal.valueOf(snInntektPrÅr))
                .build());
        }
    }

    private void byggATFL(Double frilansInntektPrÅr, Double arbeidsinntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder, Double flUtbetalingsgrad) {
        if (frilansInntektPrÅr != null || arbeidsinntektPrÅr != null) {
            BeregningsgrunnlagPrStatus.Builder atflStatusBuilder = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL);
            if (frilansInntektPrÅr != null) {
                BeregningsgrunnlagPrArbeidsforhold flAndel = BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                    .medBeregnetPrÅr(BigDecimal.valueOf(frilansInntektPrÅr))
                    .medAndelNr(2L)
                    .medUtbetalingsprosent(BigDecimal.valueOf(flUtbetalingsgrad))
                    .build();
                flAndel.setErSøktYtelseFor(flUtbetalingsgrad != null);
                atflStatusBuilder.medArbeidsforhold(flAndel);
            }
            if (arbeidsinntektPrÅr != null) {
                BeregningsgrunnlagPrArbeidsforhold arbfor = BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR))
                    .medBeregnetPrÅr(BigDecimal.valueOf(arbeidsinntektPrÅr))
                    .medAndelNr(3L)
                    .build();
                arbfor.setErSøktYtelseFor(false);
                atflStatusBuilder.medArbeidsforhold(arbfor);
            }
            periodeBuilder.medBeregningsgrunnlagPrStatus(atflStatusBuilder.build());
        }
    }
}
