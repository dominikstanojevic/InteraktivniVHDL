library ieee;
use ieee.std_logic_1164.all;

entity x is port (
 a,b: in std_logic_vector(3 downto 0);
 cin: in std_logic;
 s: out std_logic_vector(3 downto 0);
 cout: out std_logic
);
end x;

architecture str of x is
  signal co1, co2, k: std_logic;
  signal isum: std_logic_vector(3 downto 0);
  signal korekcija: std_logic_vector(3 downto 0);
begin
  z1: entity work.adder_4_bit port map (a, b, cin, isum, co1);
  k <= co1 or (isum(3) and isum(2)) or (isum(3) and isum(1));
  korekcija(0) <= '0';
  korekcija(1) <= k;
  korekcija(2) <= k;
  korekcija(3) <= '0';
  z2: entity work.adder_4_bit port map (isum, korekcija, '0', s, co2);
  cout <= k;
end str;