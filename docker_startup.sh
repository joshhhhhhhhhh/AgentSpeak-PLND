cd ../PLANNERS/touist-service || exit
eval $(opam env)
python3 -m server &
cd ../epistemic-reasoner || exit
npm start &
cd ../../AGENTSPEAK-PLND || exit
sed -i -e 's/\r$//' gradlew
./gradlew
