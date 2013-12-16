tar -czvf war.tar.gz war &&
scp war.tar.gz root@londonsales.com.au: &&
ssh root@londonsales.com.au "sh ~/deployDash.sh"
