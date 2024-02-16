package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.RegelForeslåBeregningsgrunnlagTY;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Foreslå beregningsgrunnlag
 * Beregner foreslått beregningsgrunnlag for statuser som skal beregnes i steg {@link no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå.RegelFortsettForeslåBeregningsgrunnlag}
 * Dette vil si statusene arbeidstaker, frilanser, dagpenger, arbeidsavklaringspenger og ytelser.
 * Hvis en kombinasjonsstatus består av to statuser beregnes i ulike steg, beregnes de delvis i neste steg og delvis i dette.
 * ATFL beregnes etter §8-28 og §8-30
 * ATFL_SN beregnes §8-41, som i denne regelen beregner AT/FL etter §8-28 og §8-30
 * AAP beregnes etter §14-7 2.ledd
 * DP beregnes etter §8-49
 * MIDL_INAKTIV beregnes etter §8-47
 */
public class RegelForeslåBeregningsgrunnlag implements EksportRegel<BeregningsgrunnlagPeriode> {

	public static final String ID = "BG-FORESLÅ";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
		this.regelmodell = regelmodell;
	}

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BeregningsgrunnlagPeriode> getSpecification() {
		Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

		// Fastsett alle BG per status
		var speclist = regelmodell.getAktivitetStatuser().stream()
				.map(AktivitetStatusMedHjemmel::getAktivitetStatus)
				.map(this::velgSpecification)
				.toList();
		Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag =
				rs.beregningsRegel("FP_BR pr status", "Fastsett beregningsgrunnlag pr status", speclist, new Beregnet());

		return foreslåBeregningsgrunnlag;
	}

	private Specification<BeregningsgrunnlagPeriode> velgSpecification(AktivitetStatus aktivitetStatus) {
		if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
			return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
		}
		var sporingsproperty = new ServiceArgument("aktivitetStatus", aktivitetStatus);

		if (aktivitetStatus.erAAPellerDP()) {
			return new RegelFastsettBeregningsgrunnlagDPellerAAP().getSpecification().medEvaluationProperty(sporingsproperty);
		}

		// Fastsett alle BG per status
		Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag;
		if (regelmodell.skalSplitteSammenligningsgrunnlagToggle()) {
			return switch (aktivitetStatus) {
				case MS, SN, MIDL_INAKTIV -> new Beregnet(); // Håndteres i RegelFortsettForeslåBeregningsgrunnlag
				case ATFL, ATFL_SN ->
						new RegelForeslåBeregningsgrunnlagPrStatusATFLSplitt(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
				case KUN_YTELSE ->
						new RegelForeslåBeregningsgrunnlagTY(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
				default ->
						new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification().medEvaluationProperty(sporingsproperty);
			};
		} else {
			return switch (aktivitetStatus) {
				case MS, SN, MIDL_INAKTIV -> new Beregnet(); // Håndteres i RegelFortsettForeslåBeregningsgrunnlag
				case ATFL, ATFL_SN ->
						new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
				case KUN_YTELSE ->
						new RegelForeslåBeregningsgrunnlagTY(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
				default ->
						new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification().medEvaluationProperty(sporingsproperty);
			};

		}

		return switch (aktivitetStatus) {
			case MS, SN, MIDL_INAKTIV -> new Beregnet(); // Håndteres i RegelFortsettForeslåBeregningsgrunnlag
			case ATFL, ATFL_SN ->
					new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
			case KUN_YTELSE ->
					new RegelForeslåBeregningsgrunnlagTY(regelmodell).getSpecification().medEvaluationProperty(sporingsproperty);
			default ->
					new RegelForeslåBeregningsgrunnlagTilNull(aktivitetStatus).getSpecification().medEvaluationProperty(sporingsproperty);
		};
	}
}
