package no.nav.folketrygdloven.demo;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.JsonOutput;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

class DemoFastsettBeregningsgrunnlag {

    public static final int SERVER_PORT = 5555;

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    private static final String ORGNR1 = "123";
    private static final String ORGNR2 = "456";
    private static final String ORGNR3 = "789";

    private static final String[] ORGNRS = {ORGNR1, ORGNR2, ORGNR3};

	public static void main(String[] args) {
		Spark.port(SERVER_PORT);
		Spark.staticFileLocation("/public");
		Spark.get("/api", createTestFastsettSTGetRoute(), new JsonTransformer());
		Spark.get("/rules", createRuleDescGetRoute());
		Spark.get("/doc", createDocGetRoute());
	}

	private static Route createTestFastsettSTGetRoute() {
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = prepareModell();
        return (request, response) -> {
			response.status(200);
			response.type("application/json");
			return new RegelFullføreBeregningsgrunnlag(beregningsgrunnlagPeriode).evaluer(beregningsgrunnlagPeriode);
		};
	}

	private static Route createRuleDescGetRoute() {
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = prepareModell();
		return (request, response) -> {
			response.status(200);
			response.type("application/json");
			return EvaluationSerializer.asJson(new RegelFullføreBeregningsgrunnlag(beregningsgrunnlagPeriode).evaluer(beregningsgrunnlagPeriode));
		};
	}

	private static Route createDocGetRoute() {
		return (request, response) -> {
			response.status(200);
			response.type("application/json");
			Node process = NodeConverterService.convert(new RegelFullføreBeregningsgrunnlag(prepareModell()).getSpecification().ruleDescription());
            return JsonOutput.asJson(process);
		};
	}

	private static BeregningsgrunnlagPeriode prepareModell() {
        //Arrange
        double bruttoBG1 = 200000d;
        double refusjonskrav1 = 200000d;
        double bortfaltYtelse1 = 12000d;

        double bruttoBG2 = 250000d;
        double refusjonskrav2 = 150000d;
        double bortfaltYtelse2 = 25000d;

        double totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2;
        double forventetRedusertBrukersAndel1 = bruttoBG1 + bortfaltYtelse1 - refusjonskrav1;
        double forventetRedusertBrukersAndel2 = bruttoBG2 + bortfaltYtelse2 - refusjonskrav2;
        double forventetRedusertArbeidsgiver1 = refusjonskrav1;
        double forventetRedusertArbeidsgiver2 = refusjonskrav2;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, List.of(bruttoBG1, bruttoBG2),
                List.of(refusjonskrav1 / 12, refusjonskrav2 / 12),
                List.of(bortfaltYtelse1, bortfaltYtelse2))
                .getBeregningsgrunnlagPerioder().get(0);

		return grunnlag;
	}

     private static Beregningsgrunnlag opprettGrunnlag(BeregningsgrunnlagPeriode periode) {
        Beregningsgrunnlag.Builder grunnlagsBuilder = Beregningsgrunnlag.builder()
                .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null)))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100);
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        arbeidsforhold.forEach(af -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
                .medArbeidsgiver(af.getArbeidsforhold())
                .medMåned(skjæringstidspunkt)
                .medInntekt(af.getBruttoPrÅr().get())
                .build()));
        grunnlagsBuilder.medBeregningsgrunnlagPeriode(periode)
                .medGrunnbeløpSatser(List.of(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099, Month.DECEMBER, 31), GRUNNBELØP_2017, GRUNNBELØP_2017)))
                .medInntektsgrunnlag(inntektsgrunnlag);
        return grunnlagsBuilder.build();
    }

     private static Beregningsgrunnlag lagBeregningsgrunnlagMedBortfaltNaturalytelse(int antallArbeidsforhold, List<Double> bruttoBG, List<Double> refusjonsKrav, List<Double> bortfalteYtelserPerArbeidsforhold) {
        assertThat(bruttoBG).hasSize(antallArbeidsforhold);
        assertThat(refusjonsKrav).hasSize(antallArbeidsforhold);
        assertThat(bortfalteYtelserPerArbeidsforhold).hasSize(antallArbeidsforhold);
        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(skjæringstidspunkt, null));

        BeregningsgrunnlagPrStatus.Builder prStatusBuilder = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL);
        for (int i = 0; i < antallArbeidsforhold; i++) {
            BeregningsgrunnlagPrArbeidsforhold afBuilder = BeregningsgrunnlagPrArbeidsforhold.builder()
                    .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNRS[i]))
                    .medAndelNr(i + 1)
                    .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(i)))
                    .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(bortfalteYtelserPerArbeidsforhold.get(i)))
                    .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(i) * 12))
                    .build();
            prStatusBuilder.medArbeidsforhold(afBuilder);
        }

        BeregningsgrunnlagPrStatus bgpsATFL1 = prStatusBuilder.build();
        BeregningsgrunnlagPeriode periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL1)
                .build();

        return opprettGrunnlag(periode);
    }

    static private BeregningsgrunnlagPrStatus lagBeregningsgrunnlagPrStatus(String orgNr, double brutto, int andelNr,
                                                                     AktivitetStatus aktivitetStatus, double refusjonsKrav) {
        BeregningsgrunnlagPrArbeidsforhold afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgNr))
                .medBeregnetPrÅr(BigDecimal.valueOf(brutto))
                .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav))
                .medAndelNr(andelNr)
                .build();
        return BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(aktivitetStatus)
                .medAndelNr(aktivitetStatus.equals(AktivitetStatus.ATFL) ? null : Integer.toUnsignedLong(andelNr))
                .medArbeidsforhold(afBuilder1)
                .build();
    }


	public static class JsonTransformer implements ResponseTransformer {
		@Override
		public String render(Object o) {
		    return JsonOutput.asJson(o);
		}
	}


}
