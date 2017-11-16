library ieee;
use ieee.std_logic_1164.all;


--ovo su komentari
--trebali bi biti zanemareni
ENTITY Adder_4_bit IS PORT(
	x: in std_logic_vector(3 downto 0); --prvi broj
	y: in std_logic_vector(3 downto 0); -- drugi broj
	cin: in std_logic; 
	sum: out std_logic_vector(3 downto 0);
	cout: out std_logic
);
END Adder_4_bit;

architecture arch OF Adder_4_bit IS
	signal carry: std_logic_vector(2 downto 0);
BEGIN
	a_1: ENTITY work.Adder_1_bit port map (x(0), y(0), cin, sum(0), carry(0));
	a_2: ENTITY work.Adder_1_bit port map (x(1), y(1), carry(0), sum(1), carry(1));
	a_3: ENTITY work.Adder_1_bit port map (x(2), y(2), carry(1), sum(2), carry(2));
	a_4: ENTITY work.Adder_1_bit port map (x(3), y(3), carry(2), sum(3), cout);
END arch;
--kraj