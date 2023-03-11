#!/bin/bash
# Exit on any errors
set -e

echo "We should automate this or use a github action, but for now... make sure you're on main and you've pushed everything"
read -n 1 -p "Press any key to continue..."

VER=v$(date +"%Y-%m-%dT%H.%M.%S") # EG v2023-03-11T22.35.21

echo "Creating release $VER"


echo "Building output"
./gradlew clean build

echo "Creating tag..."
git tag -a "$VER" -m "Release $VER"
echo "Pushing tag..."
git push origin "$VER"

echo "Creating github release"
gh release create "$VER" 'build/distributions/unciv-notifier.zip'