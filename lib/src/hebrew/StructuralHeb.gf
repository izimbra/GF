concrete StructuralHeb of Structural = CatHeb ** 
  open MorphoHeb, ResHeb, ParadigmsHeb, Prelude in {

  flags optimize=all ;  coding=utf8 ;

  lin

  this_Quant =  {
     s =  table { 
              Sg => table { Masc =>  "hzh" ;  Fem => "hzAt" } ;               
              _ => table {_ => "hAlh" }  
             };
     
      sp = Def ;
      isDef = True ;
       isSNum = True  -- TODO  isNum
      } ;


  that_Quant = {
  s =  table { 
              Sg => table { Masc =>  "hhwA" ;  Fem => "hhyA" } ;             -- that 
              _ => table {_ => "hhN" }                                      --  those 
             };
     
      sp = Def ;
      isDef = True ;
       isSNum = True  -- TODO  isNum

  }; 

 he_Pron = mkPron "hwA" "Awtw" "bw" Masc Sg Per3  ; 
 i_Pron = mkPron "Any" "Awty" "ly" Masc Sg Per1 ; --both fem and masc nom, acc, gen
 it_Pron = mkPron "zh" "" "" Masc Sg Per1 ;
 she_Pron = mkPron "hyA" "Awth" "lh" Fem Sg Per3  ; 
 they_Pron = mkPron  "hM" "hncnw" "lhM"   Masc Pl Per1  ; 
 we_Pron = mkPron "AnHnw" "Awtnw" "lnw" Masc Pl Per1; --both fem and masc 
 youSg_Pron = mkPron "At" "" ""  Fem Sg Per2 ; -- add Masc in extra 
 youPl_Pron = mkPron "AtN" "" "" Fem Sg Per2 ;
 youPol_Pron = mkPron "AtN" "" "" Fem Sg Per2 ;

  above_Prep = mkPrep "mOl" False;
  after_Prep = mkPrep "AHry" False;
  by8agent_Prep = mkPrep "Ol ydy" False ;
  --by8means_Prep = mkPrep "" False ;
  there_Adv = mkAdv "sm" ;
  there7to_Adv = ss "lsm" ;
  there7from_Adv = ss "msm" ;
  somewhere_Adv = ss "";
--  now_Adv = ss "Oksyw";

  but_PConj = ss "Abl" ;
}