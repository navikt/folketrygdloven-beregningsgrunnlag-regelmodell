package no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak.FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak.VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT;
import static no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getRegelResultat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.RegelBeregningsgrunnlagSN;
import no.nav.fpsak.nare.evaluation.Evaluation;

class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNTest {

    private LocalDate skjæringstidspunkt;
    private Arbeidsforhold arbeidsforhold;
    private Arbeidsforhold arbeidsforhold2;
    private BigDecimal Gverdi;
    private static final BigDecimal TOLV = new BigDecimal("12");

    @BeforeEach
    void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
	    LocalDate arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato,"123");
        arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "frilans");
        Gverdi = BigDecimal.valueOf(GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonUtenVarigEndringOppgitt() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 3, 4),
            Inntektskilde.SIGRUN);

        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal beløp = Gverdi.multiply(new BigDecimal("4"));
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, inntektATFL.multiply(TOLV), beløp.subtract(TOLV.multiply(inntektATFL)));
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringOppgittUnder25ProsentAvvik() {

        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("18000");
        BigDecimal soknadInntekt = new BigDecimal("369120");
        BigDecimal sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt);
        //Avvik = 24.98%
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();


        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, inntektATFL.multiply(TOLV), sigrun.subtract(TOLV.multiply(inntektATFL)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringOppgittOver25ProsentAvvik() {

        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("80000");
        BigDecimal soknadInntekt = new BigDecimal("309240");
        BigDecimal sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt);
        //Avvik = 25.01%
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();


        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).containsExactly(VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);


        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.01)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedNaturalYtelserBortfalt() {
        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("80000");
        BigDecimal soknadInntekt = new BigDecimal("309240");
        BigDecimal naturalytelseBeløp = new BigDecimal("5000");
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(naturalytelseBeløp).multiply(TOLV).add(soknadInntekt);

        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        List<NaturalYtelse> naturalYtelser = Collections.singletonList(new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt.minusDays(1)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt)
            .medInntekt(inntektATFL)
            .medNaturalYtelser(naturalYtelser)
            .build());

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert

        RegelResultat regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).containsExactly(VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);


        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedNaturalYtelserBortfaltMedFlerePerioder() {
        // Arrange
        BigDecimal sigrun = new BigDecimal("5").multiply(Gverdi);
        BigDecimal inntektATFL = new BigDecimal("80000");
        BigDecimal soknadInntekt = new BigDecimal("309240");
        BigDecimal naturalytelseBeløp = new BigDecimal("5000");
        BigDecimal sammenligningsgrunnlag = inntektATFL.add(naturalytelseBeløp).multiply(TOLV).add(soknadInntekt);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusMonths(4);

        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        List<NaturalYtelse> naturalYtelser = List.of(new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt.minusDays(1)),
            new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt.plusMonths(4).minusDays(1)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt)
            .medInntekt(inntektATFL)
            .medNaturalYtelser(naturalYtelser)
            .build());

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        BeregningsgrunnlagPeriode andrePeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(naturalytelseOpphørFom, null))
            .build();

        kopierBeregningsgrunnlagPeriode(grunnlag, andrePeriode);

        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(andrePeriode)
            .build();

        // Act
	    Evaluation evaluationp1 = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2p1 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

	    Evaluation evaluation1p2 = new RegelBeregningsgrunnlagATFL(andrePeriode).getSpecification().evaluate(andrePeriode);
	    Evaluation evaluation2p2 = new RegelBeregningsgrunnlagSN().evaluer(andrePeriode);


        // Assert

        RegelResultat regelResultat1 = getRegelResultat(evaluation2p1, "input");
        RegelResultat regelResultat2 = getRegelResultat(evaluation2p2, "input");
        assertThat(regelResultat1.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat1.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).containsExactly(VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);


        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);

        //Verifiser andre periode
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV).multiply(BigDecimal.valueOf(2)), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(andrePeriode, sammenligningsgrunnlag, avvik);
        assertThat(regelResultat2.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedDagpenger() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 3, 4),
            Inntektskilde.SIGRUN);
        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.DP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal bruttoDP = BigDecimal.valueOf(100000);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal gjennomsnittligPGI = Gverdi.multiply(new BigDecimal("4"));
        BigDecimal beløpATFL = inntektATFL.multiply(TOLV);
        BigDecimal beløpSN = gjennomsnittligPGI.subtract(beløpATFL).subtract(bruttoDP);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringMedAAP() {
        // Arrange
        BigDecimal sigrun = BigDecimal.valueOf(540996.444434);
        BigDecimal inntektATFL = new BigDecimal("18000");
        BigDecimal soknadInntekt = new BigDecimal("600000");
        BigDecimal bruttoAAP = BigDecimal.valueOf(49500);
        BigDecimal sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt).add(bruttoAAP);
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 6, 7),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP)).medBeregnetPrÅr(bruttoAAP).build();

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal beløpATFL = inntektATFL.multiply(TOLV);
        BigDecimal beløpSN = sigrun.subtract(beløpATFL).subtract(bruttoAAP);
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringMedDP() {
        // Arrange
        BigDecimal sigrun = BigDecimal.valueOf(655438);
        BigDecimal inntektATFL = new BigDecimal("18000");
        BigDecimal soknadInntekt = new BigDecimal("420000");
        BigDecimal bruttoDP = BigDecimal.valueOf(74880);
        BigDecimal sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt).add(bruttoDP);
        BigDecimal avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(8, 9, 10),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.DP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        BigDecimal beløpATFL = inntektATFL.multiply(TOLV);
        BigDecimal beløpSN = sigrun.subtract(beløpATFL).subtract(bruttoDP);
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    void skalGiRegelmerknadForSNSomErNyIArbeidslivet() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(6, 3, 0),
            Inntektskilde.SIGRUN);
        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.ATFL_SN), Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN)).medErNyIArbeidslivet(true);

        // Act
	    Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.merknader().get(0).utfallÅrsak()).isEqualTo(FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.01)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getGjennomsnittligPGI()).isNotNull();
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getPgiListe()).hasSize(3);
    }

    @Test
    void skalIkkeBeregneSNNårFastsattAvSaksbehandler() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);
        BigDecimal inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(inntektATFL), Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold statusAT = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrStatus statusSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrArbeidsforhold.builder(statusAT).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(232323));
        BeregningsgrunnlagPrStatus.builder(statusSN).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(323232));

        // Act
        Evaluation evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
	    Evaluation evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        RegelResultat regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        Periode beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, BigDecimal.valueOf(232323), BigDecimal.valueOf(323232));
    }


    private void verifiserBeregningsgrunnlagBruttoATFL_SN(BeregningsgrunnlagPeriode grunnlag, BigDecimal beløpATFL, BigDecimal beløpSN) {
        BeregningsgrunnlagPrStatus bgpsaATFL = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        assertThat(bgpsaATFL).isNotNull();
        assertThat(bgpsaATFL.getBeregnetPrÅr()).isCloseTo(beløpATFL, within(BigDecimal.valueOf(0.01)));

        BeregningsgrunnlagPrStatus bgpsaSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsaSN.getBeregnetPrÅr()).isCloseTo(beløpSN, within(BigDecimal.valueOf(0.01)));
    }

    private void verifiserSammenligningsgrunnlag(BeregningsgrunnlagPeriode grunnlag, BigDecimal beløp, BigDecimal prosent) {
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
        assertThat(sg).isNotNull();
        assertThat(sg.getRapportertPrÅr()).isCloseTo(beløp, within(BigDecimal.valueOf(0.01)));
        assertThat(sg.getAvvikProsent()).isCloseTo(prosent, within(BigDecimal.valueOf(0.01)));
        assertThat(sg.getAvvikPromilleUtenAvrunding().setScale(0, RoundingMode.HALF_UP))
		        .isEqualByComparingTo(prosent.movePointRight(1).setScale(0, RoundingMode.HALF_UP));
    }

    private void kopierBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagPeriode kopi) {
        for (BeregningsgrunnlagPrStatus forrigeStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (forrigeStatus.erArbeidstakerEllerFrilanser()) {
                BeregningsgrunnlagPrStatus ny = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .build();
                for (BeregningsgrunnlagPrArbeidsforhold kopierFraArbeidsforhold : forrigeStatus.getArbeidsforhold()) {
                    BeregningsgrunnlagPrArbeidsforhold kopiertArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder()
                        .medArbeidsforhold(kopierFraArbeidsforhold.getArbeidsforhold())
                        .medAndelNr(kopierFraArbeidsforhold.getAndelNr())
                        .build();
                    BeregningsgrunnlagPrStatus.builder(ny).medArbeidsforhold(kopiertArbeidsforhold).build();
                }
            } else {
                BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregnetPrÅr(forrigeStatus.getBeregnetPrÅr())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .medAndelNr(forrigeStatus.getAndelNr())
                    .build();
            }
        }
    }
}
