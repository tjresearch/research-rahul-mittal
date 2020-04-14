Theorem threefourfive : 3 + 4 = 5 + 2.
Proof.
  simpl.
  exact (eq_refl 7).
Qed.

Section Functions. 
  Variable U V : Type.
  Variable f : U -> V -> Prop.

  Definition function := forall x y y',f x y -> f x y' -> y = y'.
  Definition injective := forall x x' y, f x y -> f x' y -> x = x'.
  Definition surjective := forall (v : V), exists (u : U), f u v.
  Definition total := forall (u : U), exists (v : V), f u v.

  Definition surjector := function /\ surjective.
  Definition injector := injective /\ total.
  Definition surjective_fn := function /\ surjective /\ total.
  Definition injective_fn := function /\ injective /\ total.
  Definition bijective := function /\ injective /\ surjective /\ total.

  Axiom bijective_surjector : bijective -> surjector.
  Axiom bijective_injector : bijective -> injector.
  Axiom bijective_surjective_fn : bijective -> surjective_fn.
  Axiom bijective_injective_fn : bijective -> injective_fn.
  Axiom surjector_injector_bijective : surjector -> injector -> bijective.
  Axiom surjective_injective_bijective : surjective_fn -> injective_fn -> bijective.
End Functions.

Axiom excluded_middle : forall (P : Prop), P \/ ~P.

Theorem thm_eq_sym : (forall T : Type, forall x y : T, x = y -> y = x).
Proof.
  intros T.
  intros x y.
  intros x_y.
  destruct x_y as [].
  exact (eq_refl x).
Qed.

Theorem thm_eq_trans : (forall T : Type, forall x y z : T, (x = y) -> (y = z) -> (x = z)).
Proof.
  intros T.
  intros x y z.
  intros x_y y_z.
  rewrite x_y.
  rewrite <- y_z.
  exact (eq_refl y).
Qed.

(*Section Inverses.
  Variable U V : Type.
  Variable f : U -> V -> Prop.

  Definition inverse: V -> U -> Prop := (funv u \u21d2 f u v).

  Theorem total_iff_inverse_surjective : total f <-> surjective inverse.
  Theorem surjective_iff inverse total : surjective f <-> total inverse.
  Theorem function_iff inverse injective : function f <-> injective inverse.
  Theorem injective_iff inverse function : injective f <-> function inverse.
  Theorem surjector_iff inverse injector : surjector f <-> injector inverse.
  Theorem injector_iff inverse surjector : injector f <-> surjector inverse.
  Theorem bijective_inverse : bijective f <-> bijective inverse.
End Inverses.*)

Definition pinv {T : Type} (s1 : T -> Prop) (a : T) := ~(s1 a).

Definition disjoint_bij {T : Type} (s1 : T -> Prop) (a : T) (b : sig s1 + sig (pinv s1)) :=
	match b with
	| inl (exist _ a' _) => a = a'
	| inr (exist _ a' _) => a = a'
  end.

Axiom proof_irrelevance : forall (P:Prop) (p1 p2:P), p1 = p2.

Axiom exist_eq : forall T : Type, forall s1 : T -> Prop,
	forall a b : T, a = b -> forall proof1 : (s1 a), forall proof2 : (s1 b), exist s1 a proof1 = exist s1 b proof2.

Theorem disjoint_bij_is_function : forall (T : Type) (s1 : T -> Prop), function T (sig s1 + sig (pinv s1)) (disjoint_bij s1).
Proof.
	intros T.
	intros s1.
	intros x y y'.
	intros proof_f_x_y proof_f_x_y'.
	destruct y as [ea | ea].
		destruct ea as [a proof_a_s1].
		simpl in proof_f_x_y.
		destruct y' as [ea' | ea'].
			destruct ea' as [a' proof_a_s1'].
			simpl in proof_f_x_y'.
			rewrite (exist_eq T s1 a' a (thm_eq_trans T a' x a (thm_eq_sym T x a' proof_f_x_y') proof_f_x_y) proof_a_s1' proof_a_s1).
			exact (eq_refl (inl (exist s1 a proof_a_s1))).

			destruct ea' as [a' proof_not_a_s1'].
			simpl in proof_f_x_y'.
      pose (proof_not_a_s1'' := proof_not_a_s1').
			rewrite (thm_eq_trans T a' x a (thm_eq_sym T x a' proof_f_x_y') proof_f_x_y) in proof_not_a_s1''.
			case (proof_not_a_s1'' proof_a_s1).

		destruct ea as [a proof_not_a_s1].
		simpl in proof_f_x_y.
    destruct y' as [ea' | ea'].
			destruct ea' as [a' proof_a_s1'].
			simpl in proof_f_x_y'.
      pose (proof_a_s1'' := proof_a_s1').
			rewrite (thm_eq_trans T a' x a (thm_eq_sym T x a' proof_f_x_y') proof_f_x_y) in proof_a_s1''.
			case (proof_not_a_s1 proof_a_s1'').

			destruct ea' as [a' proof_not_a_s1'].
			simpl in proof_f_x_y'.
			rewrite (exist_eq T (pinv s1) a' a (thm_eq_trans T a' x a (thm_eq_sym T x a' proof_f_x_y') proof_f_x_y) proof_not_a_s1' proof_not_a_s1).
			exact (eq_refl (inr (exist (pinv s1) a proof_not_a_s1))).
Qed.

Theorem disjoint_bij_is_injective : forall (T : Type) (s1 : T -> Prop), injective T (sig s1 + sig (pinv s1)) (disjoint_bij s1).
Proof.
	intros T.
	intros s1.
	intros x x' y.
	intros proof_f_x_y proof_f_x_y'.
  destruct y as [ea | ea].
		destruct ea as [a].
		simpl in proof_f_x_y.
		simpl in proof_f_x_y'.
		exact (thm_eq_trans T x a x' proof_f_x_y (thm_eq_sym T x' a proof_f_x_y')).

		destruct ea as [a].
		simpl in proof_f_x_y.
		simpl in proof_f_x_y'.
		exact (thm_eq_trans T x a x' proof_f_x_y (thm_eq_sym T x' a proof_f_x_y')).
Qed.

Theorem disjoint_bij_is_surjective : forall (T : Type) (s1 : T -> Prop), surjective T (sig s1 + sig (pinv s1)) (disjoint_bij s1).
	intros T.
	intros s1.
	intros y.
	
	destruct y as [ea | ea].
		destruct ea as [a].
    simpl.
		refine (ex_intro (fun u => u = a) a _).
			exact (eq_refl a).

		destruct ea as [a].
    simpl.
		refine (ex_intro (fun u => u = a) a _).
			exact (eq_refl a).
Qed.

Theorem disjoint_bij_is_total : forall (T : Type) (s1 : T -> Prop), total T (sig s1 + sig (pinv s1)) (disjoint_bij s1).
Proof.
  intros T.
  intros s1.
  intros x.
  destruct (excluded_middle (s1 x)) as [proof_s1_x | proof_not_s1_x].
    refine (ex_intro (fun v => disjoint_bij s1 x v) (inl (exist s1 x proof_s1_x)) _).
      simpl.
      exact (eq_refl x).

    refine (ex_intro (fun v => disjoint_bij s1 x v) (inr (exist (pinv s1) x proof_not_s1_x)) _).
      simpl.
      exact (eq_refl x).
Qed.


Theorem disjoint_bij_is_bijective : forall (T : Type) (s1 : T -> Prop), bijective T (sig s1 + sig (pinv s1)) (disjoint_bij s1).
Proof.
  intros T.
  intros s1.
  refine (conj _ _).
    exact (disjoint_bij_is_function T s1).
    refine (conj _ _).
      exact (disjoint_bij_is_injective T s1).
      refine (conj _ _).
        exact (disjoint_bij_is_surjective T s1).
        exact (disjoint_bij_is_total T s1).
Qed.

Section Compositions.
  Variable U V W : Type.
  Variable f : U -> V -> Prop.
  Variable g : V -> W -> Prop.
  Definition composition : U -> W -> Prop := (fun u w => exists v , f u v /\ g v w).
  Axiom bijective_composition : bijective U V f -> bijective V W g -> bijective U W composition.
  Axiom injector_injector_composition : injector U V f -> injector V W g -> injector U W composition.
  Axiom injective_injective_composition : injective_fn U V f -> injective_fn V W g -> injective_fn U W composition.
  Axiom surjector_surjector_composition : surjector U V f -> surjector V W g -> surjector U W composition.
  Axiom surjective_surjective_composition : surjective_fn U V f -> surjective_fn V W g -> surjective_fn U W composition.
End Compositions.

Section ComputableFunctions.
  Variable U V : Type.
  Variable f : U -> V.
  Inductive relationize : U -> V -> Prop :=
    relation_intro : forall u, relationize u (f u).
End ComputableFunctions. 
Section Relations. 
  Variable U V : Type. 
  Variable f : U -> V.
  Definition surj := exists f : U -> V -> Prop, surjector U V f.
  Definition inj := exists f : U -> V -> Prop, injector U V f. 
  Definition bij := exists f : U -> V -> Prop, bijective U V f.
  Axiom surj_fn : (exists f : U -> V -> Prop, surjective_fn U V f) -> surj.
  Axiom inj_fn : (exists f : U -> V -> Prop, injective_fn U V f) -> inj.
End Relations.

Require Export Setoid.

Infix " ~= " := bij (at level 70, no associativity).
Section BijSetoid.
  Axiom bij_reflexive : reflexive _ bij.
  Axiom bij_symmetric : symmetric _ bij.
  Axiom bij_transitive : transitive _ bij.
  Axiom bij_setoid : Setoid_Theory _ bij.
End BijSetoid.

Add Setoid Type bij bij_setoid as bij_equiv.

Section RelationResults.
  Axiom bij_surj : forall U V : Type, U ~= V -> surj U V.
  Axiom bij_inj : forall U V : Type, U ~= V -> inj U V.
  Axiom surj_inj_symmetric : forall U V : Type, surj U V <-> inj V U.
  Axiom surj_inj_bij : forall U V : Type, surj U V -> inj U V -> U ~= V.
  Axiom inj_inj_bij : forall U V : Type, inj U V -> inj V U -> U ~= V.
  Axiom surj_surj_bij : forall U V : Type, surj U V -> surj V U -> U ~= V.
  Axiom inj_transitive : forall U V W : Type, inj U V -> inj V W -> inj U W.
  Axiom surj_transitive : forall U V W : Type, surj U V -> surj V W -> surj U W.
  Axiom injective_fn_from_surjective_fn : forall U V : Type, (exists f : U -> V -> Prop, surjective_fn U V f) -> exists g : V -> U -> Prop, injective_fn V U g.
End RelationResults.

Record cardinality := {
  right_size :> Type -> Prop;
  existence : exists S, right_size S;
  all_bij : forall U V, right_size U -> right_size V -> U ~= V;
  closed_under_bij : forall U V, right_size U -> U ~= V -> right_size V;
}.

Axiom cardinality_eq : forall c1 c2 : cardinality, right_size c1 = right_size c2 -> c1 = c2.
Definition cardinality_of (A : Type) : cardinality.
  refine {| right_size := bij A |}; intros.
  exists A. auto.
  apply bij_transitive with A; auto.
  apply bij_transitive with U; auto.
Defined.
Axiom cardeq_bijAB_:|_A_|=|_B_|_<->_A_~=_B._
Definition zero_cardinal := | Empty set |.
Definition cardinal_le (c1 c2 : cardinality) := exists (A : Type), c1 A /\ exists (B : Type), c2 B /\ injAB.
Axiom cardinal_le_transitive : transitive cardinal le.
Definition cardinal_ge (c1 c2 : cardinality) := c2 <= c1.
Definition cardinal_lt (c1 c2 : cardinality) := c1 <= c2 /\ c1 \u0338= c2.
Definition cardinality_gt (c1 c2 : cardinality) := c2 < c1.
Axiom cardinal_le_inj : forall A B : Type, | A | <= | B | <-> injAB.
Axiom cardinal_ge_surj : forall A B : Type, | A | >= | B | <-> surj A B. 