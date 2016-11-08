entity MaliDek2 is port (
   a0, e: in std_logic;
   i0, i1: out std_logic);
end MaliDek2;

architecture arch of MaliDek2 is
begin
   i0 <= e and not a0;
   i1 <= e and a0;
end arch;
