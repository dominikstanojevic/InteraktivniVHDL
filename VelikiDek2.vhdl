entity VelikiDek2 is port (
   a: in std_logic_vector(1 downto 0);
   e: in std_logic;
   i: out std_logic_vector(0 to 3));
end VelikiDek2;

architecture strukturna of VelikiDek2 is
   signal ez: std_logic_vector(1 to 2);
begin

   d0: entity work.MaliDek2 port map (a(1), e, ez);
   d1: entity work.MaliDek2 port map (a(0), ez(1), i(0 to 1));
   d2: entity work.MaliDek2 port map (a(0), ez(2), i(2 to 3));

end strukturna;