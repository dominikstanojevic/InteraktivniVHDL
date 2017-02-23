entity MaliDek2 is port (
   a0, e: in std_logic;
   i: out std_logic_vector(0 to 1)
   );
end MaliDek2;

architecture arch of MaliDek2 is
begin
   i(0) <= e and not a0;
   i(1) <= e and a0;
end arch;