ENTITY Adder_1_bit IS PORT(
	x: in std_logic_vector(0 to 5);
	y: in std_logic_vector(5 downto 0);
	cin: in std_logic_vector(0 to 6);
	sum: out std_logic_vector(0 to 5);
	cout: out std_logic_vector(5 downto 0)
);
END Adder_1_bit;

architecture arch OF Adder_1_bit IS
BEGIN
	sum <= x xor y xor cin(0 to 5);
	cout <= (x and y) or (x and cin(1 to 6)) or (y and cin(1 to 6));
END arch;