package no.nav.folketrygdloven.demo;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.skjæringstidspunkt.dok.DokumentasjonRegelFastsettSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.RegelFastsettSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFP;
import no.nav.folketrygdloven.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.fpsak.nare.doc.JsonOutput;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

class DemoFastsettStatus {
    public static final int SERVER_PORT = 1112;

    private static final String ARBEIDSFORHOLD = "5678";
    private static final String ARBEIDSFORHOLD2 = "5679";

    public static void main(String[] args) {
		Spark.port(SERVER_PORT);
		Spark.staticFileLocation("/public");
		Spark.get("/api", createTestFastsettSTGetRoute(), new JsonTransformer());
		Spark.get("/rules", createRuleDescGetRoute());
		Spark.get("/doc", createDocGetRoute());
	}

	private static Route createTestFastsettSTGetRoute() {
		return (request, response) -> {
			response.status(200);
			response.type("application/json");
            RegelmodellOversetter.getRegelResultat(new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(prepareModell()), "testInput");
			return new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(prepareModell());
		};
	}

	private static Route createRuleDescGetRoute() {
		return (request, response) -> {
			response.status(200);
			response.type("application/json");
			return EvaluationSerializer.asJson(new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(prepareModell()));
		};
	}

	private static Route createDocGetRoute() {
		return (request, response) -> {
			response.status(200);
			response.type("application/json");
            Node process = NodeConverterService.convert(new RegelFastsettStatusVedSkjæringstidspunkt().getSpecification().ruleDescription());
            return JsonOutput.asJson(process);
		};
	}

	private static AktivitetStatusModell prepareModell() {
		LocalDate søndag = LocalDate.of(2019, 10, 6);
		LocalDate fredag = LocalDate.of(2019, 10, 4);
		AktivPeriode aktivPeriode = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(søndag.minusMonths(5), søndag.plusMonths(2)), ARBEIDSFORHOLD, null);
		AktivPeriode aktivPeriode2 = AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(søndag.minusMonths(5), fredag.plusMonths(1)), ARBEIDSFORHOLD2, null);
		AktivPeriode aktivPeriode3 = AktivPeriode.forAndre(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, Periode.of(søndag.minusMonths(5), søndag.plusDays(4)));
		AktivPeriode aktivPeriode4 = AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(søndag.minusMonths(5), fredag.plusMonths(1)));

		AktivitetStatusModell regelmodell = new AktivitetStatusModellFP();
		regelmodell.setSkjæringstidspunktForOpptjening(søndag);
		regelmodell.setSkjæringstidspunktForBeregning(søndag);
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode3);
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode4);

		return regelmodell;
	}

	public static class JsonTransformer implements ResponseTransformer {
		@Override
		public String render(Object o) {
		    return JsonOutput.asJson(o);
		}
	}

}

