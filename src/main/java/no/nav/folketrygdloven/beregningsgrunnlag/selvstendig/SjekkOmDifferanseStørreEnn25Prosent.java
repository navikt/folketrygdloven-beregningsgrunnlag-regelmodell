package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDifferanseStørreEnn25Prosent.ID)
public class SjekkOmDifferanseStørreEnn25Prosent extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR 2.5";
	static final String NAVN = "Varig endring og avvik større enn 25%, beregningsgrunnlag fastsettes skjønnsmessig";
	private final AktivitetStatus aktivitetStatus;

	public SjekkOmDifferanseStørreEnn25Prosent(AktivitetStatus aktivitetStatus) {
		super(ID, NAVN);
		if (!AktivitetStatus.SN.equals(aktivitetStatus) && !AktivitetStatus.BA.equals(aktivitetStatus)) {
			throw new IllegalArgumentException("Kan ikke beregne avvik for aktivitetstatus " + aktivitetStatus);
		}
		this.aktivitetStatus = aktivitetStatus;
	}



    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		var type = aktivitetStatus.erSelvstendigNæringsdrivende() ? SammenligningGrunnlagType.SN : SammenligningGrunnlagType.MIDLERTIDIG_INAKTIV;
        final SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(type);
        return (sg.getAvvikProsent().compareTo(grunnlag.getAvviksgrenseProsent()) > 0 ? ja() : nei());
    }
}
