pdfs:
	read -p username:\  user && read -sp password:\  pass && mkdir -p pdfs && cd pdfs && curl 'http://plantoperations.uwaterloo.ca/floor_plans/' | sed -n 's/.*<a href="\([^"]*\.pdf\)".*/\1/p' | grep -Fv /restricted/ | xargs -P5 -n1 curl --user "$$user:$$pass" -gLOC -
pngs: pdfs
	cd pdfs && for pdf in *.pdf; do convert "$$pdf" -background white -flatten "$${pdf%pdf}png"; done
	cd pdfs && for pdf in *.pdf; do convert "$$pdf" -background white -flatten "$${pdf%pdf}gif"; done #XXX
	cd pdfs && find -name \*.png -exec optipng -o7 {} + -exec advpng -z4 {} + -exec optipng -o7 {} +
links:
	for gif in pdfs/*.gif; do echo "processing $$gif"; ./get_labels.py "$$gif" "$${gif%gif}json" || break; done
clean-unauthorized:
	cd pdfs && grep Auth * | grep : | cut -d: -f0 | uniq | xargs rm
names.txt:
	curl 'http://plantoperations.uwaterloo.ca/floor_plans/' | sed -n 's/.*<a href="\([^"]*\.pdf\)".*/\1/p' | sed -n 's,.*/[0-9][0-9][0-9]\([^_]*\)_.*\([0-9][0-9]*\)FLR\.pdf,\1 \2,p' | uniq > names.txt
.PHONY: links pngs clean-unauthorized
