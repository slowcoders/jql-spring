pushd basic-sample-app
sh ../db/start_postgres.sh
./gradlew bootRun &
popd

until $(curl --output /dev/null --silent --head --fail http://localhost:6090/api/jql/starwars/hello); do
    printf '.'
    sleep 5
done
pushd tutorial
npm install
npm run serve
popd