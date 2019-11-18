import Data.List (intercalate)

data Formula = Constant Integer | Variable Char | Sum [Formula] | Product [Formula] | Combination Formula Formula | Factorial Formula | Difference Formula Formula | Quotient Formula Formula deriving Show

Sum xs +++ b = Sum (xs ++ [b])
a +++ Sum ys = Sum (a:ys)
a +++ b = Sum [a, b]

Product xs *** b = Product (xs ++ [b])
a *** Product ys = Product (a:ys)
a *** b = Product [a, b]

(>>) = Combination
fact = Factorial

jsonObject tp ps = "{\"type\": \"" ++ tp ++ "\", " ++ intercalate ", " ["\"" ++ key ++ "\": " ++ value | (key, value) <- ps] ++ "}"
jsonArray js = "[" ++ intercalate ", " js ++ "]"

-- k for konstant!
json (Constant k) = show k
json (Variable chara) = '"':chara:'"':""
json (Sum fs) = jsonObject "sum" [("addends", jsonArray (map json fs))]
json (Product fs) = jsonObject "product" [("mands", jsonArray (map json fs))]
json (Combination n k) = jsonObject "choose" [("top", json n), ("bottom", json k)]
json (Factorial f) = jsonObject "factorial" [("of", json f)]
json (Difference f1 f2) = jsonObject "difference" [("left", json f1), ("right", json f2)]
json (Quotient f1 f2) = jsonObject "quotient" [("top", json f1), ("bottom", json f2)]