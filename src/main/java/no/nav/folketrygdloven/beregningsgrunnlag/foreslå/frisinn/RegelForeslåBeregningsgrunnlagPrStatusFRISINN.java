package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlagTilNull;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagPrStatusFRISINN.ID, specificationReference = "https://confluence.adeo.no/display/SIF/30.+Beregningsgrunnlag")
public class RegelForeslåBeregningsgrunnlagPrStatusFRISINN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "BG-PR-STATUS";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        ServiceArgument arg = getServiceArgument();
        if (arg == null || !(arg.getVerdi() instanceof AktivitetStatusMedHjemmel)) {
            throw new IllegalStateException("Utviklerfeil: AktivitetStatus må angis som parameter");
        }
        if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
            return new IkkeBeregnet(new RuleReasonRefImpl("x", "y"));
        }
        AktivitetStatus aktivitetStatus = ((AktivitetStatusMedHjemmel) arg.getVerdi()).getAktivitetStatus();
        if (AktivitetStatus.ATFL.equals(aktivitetStatus)) {
            return new RegelBeregningsgrunnlagATFLFRISINN(regelmodell).getSpecification().medScope(arg);
        } else if (AktivitetStatus.SN.equals(aktivitetStatus)) {
            return new RegelBeregningsgrunnlagSNFRISINN().getSpecification().medScope(arg);
        } else if (AktivitetStatus.ATFL_SN.equals(aktivitetStatus)) {
            RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN regelFastsetteBeregningsgrunnlagForKombinasjonATFLSN = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNFRISINN(regelmodell);
            return regelFastsetteBeregningsgrunnlagForKombinasjonATFLSN.getSpecification().medScope(arg);
        } else if (AktivitetStatus.AAP.equals(aktivitetStatus) || AktivitetStatus.DP.equals(aktivitetStatus)) {
            return new ForeslåBeregningsgrunnlagDPellerAAPFRISINN();
        }

        return new RegelForeslåBeregningsgrunnlagTilNull().medServiceArgument(arg).getSpecification();
    }
}
