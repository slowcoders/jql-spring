pushd basic-sample-app/tutorial
npm install
npm run build
popd
pushd basic-sample-app
sh ./db/start_postgres.sh
./gradlew bootRun --console=plain
popd
