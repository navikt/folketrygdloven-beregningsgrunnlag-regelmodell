package no.nav.folketrygdloven.beregningsgrunnlag;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.internal.cglib.core.Local;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;

public class BeregningsgrunnlagScenario {

    private static final String ORGNR = "987";
    public static final long GRUNNBELØP_2019 = 99858;
    public static final long GRUNNBELØP_2018 = 94562;
    public static final long GRUNNBELØP_2017 = 93634;
    public static final long GSNITT_2019 = 98866;
    public static final long GSNITT_2018 = 94725;
    public static final long GSNITT_2017 = 93281;
    public static final long GSNITT_2016 = 91740;
    public static final long GSNITT_2015 = 89502;
    private static final long GSNITT_2014 = 87328;
    private static final long GSNITT_2013 = 84204;

    public static final List<Grunnbeløp> GRUNNBELØPLISTE = List.of(
        new Grunnbeløp(LocalDate.of(2013, 5, 1), LocalDate.of(2014, 4, 30), 85245L, GSNITT_2013),
        new Grunnbeløp(LocalDate.of(2014, 5, 1), LocalDate.of(2015, 4, 30), 88370L, GSNITT_2014),
        new Grunnbeløp(LocalDate.of(2015, 5, 1), LocalDate.of(2016, 4, 30), 90068L, GSNITT_2015),
        new Grunnbeløp(LocalDate.of(2016, 5, 1), LocalDate.of(2017, 4, 30), 92576L, GSNITT_2016),
        new Grunnbeløp(LocalDate.of(2017, 5, 1), LocalDate.of(2018, 4, 30), GRUNNBELØP_2017, GSNITT_2017),
        new Grunnbeløp(LocalDate.of(2018, 5, 1), LocalDate.of(2019,4,30), GRUNNBELØP_2018, GSNITT_2018),
        new Grunnbeløp(LocalDate.of(2019, 5, 1), LocalDate.MAX, 99858L, GSNITT_2019));


    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt, Inntektsgrunnlag inntektsgrunnlag, List<AktivitetStatus> aktivitetStatuser) {
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, aktivitetStatuser, Collections.emptyList(), Collections.emptyList());
    }

    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt, Inntektsgrunnlag inntektsgrunnlag, List<AktivitetStatus> aktivitetStatuser, Optional<YtelsesSpesifiktGrunnlag> ytelsesSpesifiktGrunnlag) {
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, aktivitetStatuser, Collections.emptyList(), Collections.emptyList(), ytelsesSpesifiktGrunnlag);
    }

    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt,
                                                                 Inntektsgrunnlag inntektsgrunnlag,
                                                                 List<AktivitetStatus> aktivitetStatuser,
                                                                 List<Arbeidsforhold> arbeidsforhold) {
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, aktivitetStatuser, arbeidsforhold, Collections.emptyList());
    }

    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt,
                                                                 Inntektsgrunnlag inntektsgrunnlag,
                                                                 List<AktivitetStatus> aktivitetStatuser,
                                                                 List<Arbeidsforhold> arbeidsforhold,
                                                                 Optional<YtelsesSpesifiktGrunnlag> ytelsesSpesifiktGrunnlag) {
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, aktivitetStatuser, arbeidsforhold, Collections.emptyList(), ytelsesSpesifiktGrunnlag);
    }

    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt,
                                                                 Inntektsgrunnlag inntektsgrunnlag,
                                                                 List<AktivitetStatus> aktivitetStatuser,
                                                                 List<Arbeidsforhold> arbeidsforhold,
                                                                 List<BigDecimal> refusjonskravPrÅr) {
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, aktivitetStatuser, arbeidsforhold, Collections.emptyList(), refusjonskravPrÅr);
    }

    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt,
                                                                 Inntektsgrunnlag inntektsgrunnlag,
                                                                 List<AktivitetStatus> aktivitetStatuser,
                                                                 List<Arbeidsforhold> arbeidsforhold,
                                                                 List<BigDecimal> refusjonskravPrÅr,
                                                                 Optional<YtelsesSpesifiktGrunnlag> ytelsesSpesifiktGrunnlag) {
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, aktivitetStatuser, arbeidsforhold, Collections.emptyList(), refusjonskravPrÅr, ytelsesSpesifiktGrunnlag);
    }

    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt,
                                                                 Inntektsgrunnlag inntektsgrunnlag,
                                                                 List<AktivitetStatus> aktivitetStatuser,
                                                                 List<Arbeidsforhold> arbeidsforhold,
                                                                 List<PeriodeÅrsak> periodeÅrsaker,
                                                                 List<BigDecimal> refusjonskravPrår) {
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, aktivitetStatuser, arbeidsforhold, periodeÅrsaker, refusjonskravPrår, Optional.empty());
    }

    public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt,
                                                                 Inntektsgrunnlag inntektsgrunnlag,
                                                                 List<AktivitetStatus> aktivitetStatuser,
                                                                 List<Arbeidsforhold> arbeidsforhold,
                                                                 List<PeriodeÅrsak> periodeÅrsaker,
                                                                 List<BigDecimal> refusjonskravPrår,
                                                                 Optional<YtelsesSpesifiktGrunnlag> ytelsesSpesifiktGrunnlag) {

        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null))
            .medPeriodeÅrsaker(periodeÅrsaker);
        long andelNr = arbeidsforhold.size() + 1;
        for (AktivitetStatus aktivitetStatus : aktivitetStatuser) {
            if (AktivitetStatus.ATFL_SN.equals(aktivitetStatus)) {
                BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medArbeidsforhold(arbeidsforhold, refusjonskravPrår, skjæringstidspunkt)
                    .build();
                BeregningsgrunnlagPrStatus bgpsSN = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(AktivitetStatus.SN)
                    .medAndelNr(andelNr++)
                    .build();
                periodeBuilder.medBeregningsgrunnlagPrStatus(bgpsATFL).medBeregningsgrunnlagPrStatus(bgpsSN);
            } else if (AktivitetStatus.KUN_YTELSE.equals(aktivitetStatus)) {
                BeregningsgrunnlagPrStatus bgpsBA = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(AktivitetStatus.BA)
                    .medAndelNr(andelNr++)
                    .build();
                periodeBuilder.medBeregningsgrunnlagPrStatus(bgpsBA);
            } else {
                BeregningsgrunnlagPrStatus.Builder bgps = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(aktivitetStatus);
                if (AktivitetStatus.erArbeidstakerEllerFrilanser(aktivitetStatus)) {
                    bgps.medArbeidsforhold(arbeidsforhold, refusjonskravPrår, skjæringstidspunkt);
                } else {
                    bgps.medAndelNr(andelNr++);
                }
                periodeBuilder.medBeregningsgrunnlagPrStatus(bgps.build());
            }
        }
        BeregningsgrunnlagPeriode bgPeriode = periodeBuilder.build();
        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(inntektsgrunnlag)
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
            .medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
            .medAktivitetStatuser(aktivitetStatuser.stream().map(as -> new AktivitetStatusMedHjemmel(as, null)).collect(Collectors.toList()))
            .medBeregningsgrunnlagPeriode(bgPeriode)
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .medYtelsesSpesifiktGrunnlag(ytelsesSpesifiktGrunnlag.orElse(new ForeldrepengerGrunnlag(false)))
            .build();
    }

    public static Beregningsgrunnlag settOppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt, Inntektsgrunnlag inntektsgrunnlag, AktivitetStatus aktivitetStatus, List<Arbeidsforhold> arbeidsforhold, List<BigDecimal> refusjonskravPrår, boolean skalSjekkeRefusjonFørSetteAksjonspunkt, BigDecimal maksRefusjon) {
        BeregningsgrunnlagPrStatus bgps = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medArbeidsforhold(arbeidsforhold, refusjonskravPrår, skjæringstidspunkt)
            .build();
        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPrStatus(bgps)
            .medPeriode(Periode.of(skjæringstidspunkt, null));

        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(inntektsgrunnlag)
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
            .medYtelsesSpesifiktGrunnlag(skalSjekkeRefusjonFørSetteAksjonspunkt ? new OmsorgspengerGrunnlag(maksRefusjon) : null)
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(aktivitetStatus, null)))
            .medBeregningsgrunnlagPeriode(periodeBuilder.build())
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .build();
    }

    private static Inntektsgrunnlag settoppÅrsinntekterATFL(LocalDate skjæringstidspunkt, List<BigDecimal> årsinntekt, Inntektskilde inntektskilde) {
        LocalDate førsteMåned = skjæringstidspunkt.minusYears(årsinntekt.size()).withMonth(1).withDayOfMonth(1);
        int år = 0;
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        for (BigDecimal beløp : årsinntekt) {
            BigDecimal inntekt = BigDecimal.valueOf(beløp.doubleValue() / 12);
            for (int måned = 0; måned < 12; måned++) {
                inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                    .medInntektskildeOgPeriodeType(inntektskilde)
                    .medMåned(førsteMåned.plusYears(år).plusMonths(måned))
                    .medInntekt(inntekt)
                    .build());
            }
            år++;
        }
        return inntektsgrunnlag;
    }

    private static Inntektsgrunnlag settoppÅrsinntekterSigrun(LocalDate skjæringstidspunkt, List<BigDecimal> årsinntekter) {
        int år = skjæringstidspunkt.minusYears(årsinntekter.size()).getYear();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        for (BigDecimal beløp : årsinntekter) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
                .medPeriode(Periode.of(LocalDate.of(år, 1,1), LocalDate.of(år, 12, 31)))
                .medInntekt(beløp)
                .build());
            år++;
        }
        return inntektsgrunnlag;
    }

    public static Inntektsgrunnlag settoppÅrsinntekter(LocalDate skjæringstidspunkt, List<BigDecimal> årsinntekt, Inntektskilde inntektskilde) {
        if (inntektskilde == Inntektskilde.SIGRUN) {
            return settoppÅrsinntekterSigrun(skjæringstidspunkt, årsinntekt);
        } else {
            return settoppÅrsinntekterATFL(skjæringstidspunkt, årsinntekt, inntektskilde);
        }
    }

    public static Inntektsgrunnlag settoppÅrsinntekterForOppgittÅrene(List<BigDecimal> årsinntekt, Inntektskilde inntektskilde, int... åretArray) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        for (int ix = 0; ix < åretArray.length; ix++) {
            BigDecimal inntekt = BigDecimal.valueOf(årsinntekt.get(ix).doubleValue() / 12);
            LocalDate førsteMåned = LocalDate.of(åretArray[ix], 1, 1);
            for (int måned = 0; måned < 12; måned++) {
                inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                    .medInntektskildeOgPeriodeType(inntektskilde)
                    .medMåned(førsteMåned.plusMonths(måned))
                    .medInntekt(inntekt)
                    .build());
            }
        }
        return inntektsgrunnlag;
    }

    public static Inntektsgrunnlag settoppMånedsinntekter(LocalDate skjæringstidspunkt, List<BigDecimal> månedsinntekt, Inntektskilde inntektskilde, Arbeidsforhold arbeidsforhold) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekt, inntektskilde, arbeidsforhold);
        return inntektsgrunnlag;
    }

    public static Inntektsgrunnlag settoppMånedsinntekterPrAktivitet(LocalDate skjæringstidspunkt, List<BigDecimal> månedsinntekt, Inntektskilde inntektskilde, Arbeidsforhold arbeidsforhold, AktivitetStatus aktivitetStatus) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekt, inntektskilde, arbeidsforhold, aktivitetStatus);
        return inntektsgrunnlag;
    }

    public static void leggTilMånedsinntekter(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, List<BigDecimal> månedsinntekt, Inntektskilde inntektskilde, Arbeidsforhold arbeidsforhold, AktivitetStatus aktivitetStatus) {
        int månederSiden = månedsinntekt.size();
        for (BigDecimal beløp : månedsinntekt) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                    .medInntektskildeOgPeriodeType(inntektskilde)
                    .medArbeidsgiver(arbeidsforhold)
                    .medMåned(skjæringstidspunkt.minusMonths(månederSiden))
                    .medInntekt(beløp)
                    .medAktivitetStatus(aktivitetStatus)
                    .build());
            månederSiden--;
        }
    }

    public static void leggTilMånedsinntekter(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, List<BigDecimal> månedsinntekt, Inntektskilde inntektskilde, Arbeidsforhold arbeidsforhold) {
        int månederSiden = månedsinntekt.size();
        for (BigDecimal beløp : månedsinntekt) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(inntektskilde)
                .medArbeidsgiver(arbeidsforhold)
                .medMåned(skjæringstidspunkt.minusMonths(månederSiden))
                .medInntekt(beløp)
                .build());
            månederSiden--;
        }
    }

    public static void leggTilSøknadsinntekt(Inntektsgrunnlag inntektsgrunnlag, BigDecimal inntekt) {
        inntektsgrunnlag.leggTilPeriodeinntekt(
            Periodeinntekt.builder()
                .medInntekt(inntekt)
                .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
                .medAktivitetStatus(AktivitetStatus.SN)
                .medMåned(LocalDate.of(2019,1,1))
                .build()
        );
    }


    public static void leggTilMånedsinntekterPrStatus(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, List<BigDecimal> månedsinntekt, Inntektskilde inntektskilde, Arbeidsforhold arbeidsforhold, AktivitetStatus aktivitetStatus) {
        int månederSiden = månedsinntekt.size();
        for (BigDecimal beløp : månedsinntekt) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(inntektskilde)
                .medArbeidsgiver(arbeidsforhold)
                .medMåned(skjæringstidspunkt.minusMonths(månederSiden))
                .medInntekt(beløp)
                .medAktivitetStatus(aktivitetStatus)
                .build());
            månederSiden--;
        }
    }

    public static void kopierOgLeggTilMånedsinntekter(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, Inntektskilde inntektskilde, Arbeidsforhold arbeidsforhold, int måneder) {
        for (int månederSiden = måneder; månederSiden > 0; månederSiden--) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(inntektskilde)
                .medArbeidsgiver(arbeidsforhold)
                .medMåned(skjæringstidspunkt.minusMonths(månederSiden))
                .medInntekt(månedsinntekt)
                .build());

        }
    }

    public static void kopierOgLeggTilMånedsinntekterPrAktivitet(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, Inntektskilde inntektskilde, Arbeidsforhold arbeidsforhold, int måneder, AktivitetStatus aktivitetStatus) {
        for (int månederSiden = måneder; månederSiden > 0; månederSiden--) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                    .medInntektskildeOgPeriodeType(inntektskilde)
                    .medArbeidsgiver(arbeidsforhold)
                    .medMåned(skjæringstidspunkt.minusMonths(månederSiden))
                    .medInntekt(månedsinntekt)
                    .medAktivitetStatus(aktivitetStatus)
                    .build());

        }
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektskomponenten(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav, boolean frilans) {
        return opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, frilans, 12);
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektskomponenten(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav, boolean frilans, List<PeriodeÅrsak> periodeÅrsaker) {
        return opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, frilans, 12, periodeÅrsaker);
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektskomponenten(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav, boolean frilans, boolean medSammenligningsgrunnlag) {
        Beregningsgrunnlag bg = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, frilans);
        if (medSammenligningsgrunnlag) {
            opprettSammenligningsgrunnlag(bg.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        }
        return bg;
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektskomponenten(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav, boolean frilans, int antallMåneder) {
        return opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, frilans, antallMåneder, Collections.emptyList());
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektskomponenten(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav, boolean frilans,
                                                                                     int antallMåneder, List<PeriodeÅrsak> periodeÅrsaker) {
        Arbeidsforhold arbeidsforhold = frilans ? Arbeidsforhold.frilansArbeidsforhold() : Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
        List<BigDecimal> månedsinntekter = new ArrayList<>();
        for (int m = 0; m < antallMåneder; m++) {
            månedsinntekter.add(månedsinntekt);
        }
        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, singletonList(AktivitetStatus.ATFL),
            singletonList(arbeidsforhold), periodeÅrsaker, refusjonskrav == null ? Collections.emptyList() : singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektskomponentenPrAktivitet(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav, boolean frilans,
                                                                                                int antallMåneder, List<PeriodeÅrsak> periodeÅrsaker, AktivitetStatus aktivitetStatus) {
        Arbeidsforhold arbeidsforhold = frilans ? Arbeidsforhold.frilansArbeidsforhold() : Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
        List<BigDecimal> månedsinntekter = new ArrayList<>();
        for (int m = 0; m < antallMåneder; m++) {
            månedsinntekter.add(månedsinntekt);
        }
        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekterPrAktivitet(skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, aktivitetStatus);
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, singletonList(AktivitetStatus.ATFL),
                singletonList(arbeidsforhold), periodeÅrsaker, refusjonskrav == null ? Collections.emptyList() : singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));
    }

    public static void opprettSammenligningsgrunnlag(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, BigDecimal månedsinntekt) {
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Arrays.asList(månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt,
                månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null);
    }

    public static void opprettSammenligningsgrunnlagPrAktivitet(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, AktivitetStatus aktivitetStatus) {
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
                Arrays.asList(månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt,
                        månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null, aktivitetStatus);
    }

    public static void opprettSammenligningsgrunnlagIPeriode(Inntektsgrunnlag inntektsgrunnlag, Periode periode, BigDecimal månedsinntekt, AktivitetStatus aktivitetStatus) {
        for (LocalDate date = periode.getFom(); date.isBefore(periode.getTom().plusMonths(1)) ; date = date.plusMonths(1)) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
                .medMåned(date)
                .medInntekt(månedsinntekt)
                .medAktivitetStatus(aktivitetStatus)
                .build());
        }
    }

    public static void opprettInntektsmeldingIPeriode(Inntektsgrunnlag inntektsgrunnlag, Periode periode, BigDecimal månedsinntekt, AktivitetStatus aktivitetStatus, Arbeidsforhold arbeidsforhold) {
        for (LocalDate date = periode.getFom(); date.isBefore(periode.getTom().plusMonths(1)) ; date = date.plusMonths(1)) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
                .medMåned(date)
                .medInntekt(månedsinntekt)
                .medAktivitetStatus(aktivitetStatus)
                .medArbeidsgiver(arbeidsforhold)
                .build());
        }
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektsmelding(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav) {
        return opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt, refusjonskrav, BigDecimal.ZERO, null);
    }

    public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektsmelding(LocalDate skjæringstidspunkt, BigDecimal månedsinntekt, BigDecimal refusjonskrav, BigDecimal naturalytelse, LocalDate naturalytelseOpphørFom) {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        List<NaturalYtelse> naturalYtelser = lagNaturalYtelseListe(naturalytelse, naturalytelseOpphørFom);
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt.minusMonths(1))
            .medInntekt(månedsinntekt)
            .medNaturalYtelser(naturalYtelser)
            .build());
        return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, singletonList(AktivitetStatus.ATFL), singletonList(arbeidsforhold), singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));
    }


    private static List<NaturalYtelse> lagNaturalYtelseListe(BigDecimal naturalytelse, LocalDate naturalytelseOpphørFom) {
        List<NaturalYtelse> naturalYtelser = new ArrayList<>();
        if (naturalytelseOpphørFom != null) {
            naturalYtelser.add(new NaturalYtelse(naturalytelse, null, naturalytelseOpphørFom.minusDays(1)));
        }
        return naturalYtelser;
    }

    public static void leggTilArbeidsforholdMedInntektsmelding(BeregningsgrunnlagPeriode grunnlag, LocalDate skjæringstidspunkt,
                                                               BigDecimal månedsinntekt, BigDecimal refusjonskrav, Arbeidsforhold arbeidsforhold,
                                                               BigDecimal naturalytelse, LocalDate naturalytelseOpphørFom) {
        grunnlag.getInntektsgrunnlag().leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt.minusMonths(1))
            .medInntekt(månedsinntekt)
            .medNaturalYtelser(lagNaturalYtelseListe(naturalytelse, naturalytelseOpphørFom))
            .build());
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL))
            .medArbeidsforhold(singletonList(arbeidsforhold), singletonList(refusjonskrav), skjæringstidspunkt)
            .build();
    }

    public static void leggTilArbeidsforholdUtenInntektsmelding(BeregningsgrunnlagPeriode grunnlag, LocalDate skjæringstidspunkt,
                                                                BigDecimal månedsinntekt, BigDecimal refusjonskrav, Arbeidsforhold arbeidsforhold) {
        List<BigDecimal> månedsinntekter = new ArrayList<>();
        for (int m = 0; m < 12; m++) {
            månedsinntekter.add(månedsinntekt);
        }
        leggTilMånedsinntekter(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL))
            .medArbeidsforhold(singletonList(arbeidsforhold), singletonList(refusjonskrav), skjæringstidspunkt)
            .build();
    }


    public static List<BigDecimal> årsinntekterFor3SisteÅr(double pgi3, double pgi2, double pgi1) {
        return Arrays.asList(BigDecimal.valueOf(pgi3 * GSNITT_2015), BigDecimal.valueOf(pgi2 * GSNITT_2016), BigDecimal.valueOf(pgi1 * GSNITT_2017));
    }

    public static List<BigDecimal> årsinntekterFor2SisteÅr(double pgi2, double pgi1) {
        return Arrays.asList(BigDecimal.valueOf(pgi2 * GSNITT_2017), BigDecimal.valueOf(pgi1 * GSNITT_2018));
    }

    public static List<BigDecimal> årsinntektForOppgittÅrene(double pgiMultiplicand, int... åreneArray) {
        List<BigDecimal> pgiListe = new ArrayList<>();
        for (int året : åreneArray) {
            long GsnittVerdi = GRUNNBELØPLISTE.stream().filter(grunnbeløp -> grunnbeløp.getFom().getYear() == året).findFirst()
                .orElseThrow(() -> new IllegalStateException("Mangler gsnitt beløp for " + året)).getGSnitt();
            pgiListe.add(BigDecimal.valueOf(pgiMultiplicand * GsnittVerdi));
        }
        return pgiListe;
    }


}
