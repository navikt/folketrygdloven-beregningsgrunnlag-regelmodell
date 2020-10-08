package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.avkorting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0.ID)
class AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0 extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.3";
    static final String BESKRIVELSE = "Avkort alle beregningsgrunnlagsandeler som ikke gjelder arbeidsforhold til 0.";

    AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        Map<String, Object> resultater = new HashMap<>();
        grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes().stream()
            .filter(bgps -> !bgps.erArbeidstakerEllerFrilanser())
            .forEach(bgps -> {
                BeregningsgrunnlagPrStatus.builder(bgps).medAvkortetPrÅr(BigDecimal.ZERO).build();
                resultater.put("avkortetPrÅr.status." + bgps.getAktivitetStatus().name(), bgps.getAvkortetPrÅr());
            });

        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atfl != null) {
            atfl.getFrilansArbeidsforholdSomSkalBrukes().ifPresent(af -> {
                    BeregningsgrunnlagPrArbeidsforhold.builder(af).medAvkortetPrÅr(BigDecimal.ZERO).medAvkortetRefusjonPrÅr(BigDecimal.ZERO).medAvkortetBrukersAndelPrÅr(BigDecimal.ZERO).build();
                    resultater.put("avkortetPrÅr.status." + atfl.getAktivitetStatus().name() + "." + af.getArbeidsgiverId(), af.getAvkortetPrÅr());
                });
        }
        return beregnet(resultater);

    }



}
