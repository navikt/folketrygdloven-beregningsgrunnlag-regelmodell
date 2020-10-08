package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsettAvkortetVedRefusjonOver6G implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29.13";
    public static final String BESKRIVELSE = "Fastsett avkortet BG når refusjon over 6G";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelFastsettAvkortetVedRefusjonOver6G(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        BeregningsgrunnlagPrStatus bgpsa = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (bgpsa == null) {
            return new Beregnet();
        }

        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> fastsettBrukersAndelerTilNull = new FastsettBrukersAndelerTilNull();
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetBeregningsgrunnlag = new Beregnet();

        List<BeregningsgrunnlagPrArbeidsforhold> prArbeidsforhold = new ArrayList<>(bgpsa.getArbeidsforholdSomSkalBrukes());

        if (!prArbeidsforhold.isEmpty()) {
            fastsettAvkortetBeregningsgrunnlag = rs.beregningsRegel(ID, BESKRIVELSE, RegelBeregnRefusjonPrArbeidsforhold.class,
                    regelmodell, "prArbeidsforhold", prArbeidsforhold, fastsettBrukersAndelerTilNull);
        }

        return fastsettAvkortetBeregningsgrunnlag;
    }
}
