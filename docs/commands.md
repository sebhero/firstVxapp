# openshift

to push
git add -A
git commit -m "deploy my application"
git push

## Fix git lock
The solution is very easy, just delete the .git/index.lock file
~ rm /yourhomedir/.git/index.lock

Then you need to do a git reset
~ git reset