entity sklopI is port (
   a,b: in std_logic;
   f, test: out std_logic);
end sklopI;

architecture pon of sklopI is
   signal pomocni: std_logic;
begin
   pomocni <= a and b after 3 s;
   f <= pomocni after 100 ms;
   test <= pomocni after 2 s;
end pon;
